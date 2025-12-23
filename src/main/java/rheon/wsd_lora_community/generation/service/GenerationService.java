package rheon.wsd_lora_community.generation.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rheon.wsd_lora_community.generation.dto.AvailableModelResponse;
import rheon.wsd_lora_community.generation.dto.GenerateImageRequest;
import rheon.wsd_lora_community.generation.dto.GenerationHistoryResponse;
import rheon.wsd_lora_community.generation.entity.GeneratedImage;
import rheon.wsd_lora_community.generation.entity.GenerationHistory;
import rheon.wsd_lora_community.generation.repository.GeneratedImageRepository;
import rheon.wsd_lora_community.generation.repository.GenerationHistoryRepository;
import rheon.wsd_lora_community.global.dto.PageResponse;
import rheon.wsd_lora_community.global.exception.CustomException;
import rheon.wsd_lora_community.global.exception.ErrorCode;
import rheon.wsd_lora_community.global.service.S3Service;
import rheon.wsd_lora_community.global.service.S3UploadService;
import rheon.wsd_lora_community.model.entity.LoraModel;
import rheon.wsd_lora_community.model.repository.LoraModelRepository;
import rheon.wsd_lora_community.model.repository.ModelSampleRepository;
import rheon.wsd_lora_community.user.entity.User;
import rheon.wsd_lora_community.user.repository.UserRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 이미지 생성 관리 서비스
 *
 * 주의: 실제 이미지 생성은 FastAPI 서버와 통신하여 처리됩니다.
 * 이 서비스는 생성 요청 및 기록 관리만 담당합니다.
 */
@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GenerationService {

    private final GenerationHistoryRepository generationHistoryRepository;
    private final GeneratedImageRepository generatedImageRepository;
    private final ModelSampleRepository modelSampleRepository;
    private final LoraModelRepository loraModelRepository;
    private final UserRepository userRepository;
    private final S3UploadService s3UploadService;
    private final S3Service s3Service;

    /**
     * 이미지 생성 완료 처리 (FastAPI 콜백)
     *
     * @param historyId GenerationHistory ID (없으면 자동 생성)
     * @param modelId 모델 ID
     * @param userId 사용자 ID
     * @param prompt 프롬프트
     * @param negativePrompt 네거티브 프롬프트
     * @param steps 스텝 수
     * @param guidanceScale 가이던스 스케일
     * @param loraScale LoRA 강도
     * @param seed 시드
     * @param numImages 생성된 이미지 개수
     * @param imageS3Keys S3에 저장된 이미지 키 리스트
     * @return 생성 기록 응답
     */
    @Transactional
    public GenerationHistoryResponse completeGeneration(
            Long historyId,
            Long modelId,
            Long userId,
            String prompt,
            String negativePrompt,
            Integer steps,
            Double guidanceScale,
            Double loraScale,
            Long seed,
            Integer numImages,
            List<String> imageS3Keys
    ) {
        GenerationHistory history;

        if (historyId != null) {
            // 기존 GenerationHistory 업데이트
            history = generationHistoryRepository.findById(historyId)
                    .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));

            // SUCCESS 상태로 변경
            history.markAsSuccess();
        } else {
            // historyId가 없으면 새로 생성 (하위 호환성)
            LoraModel model = loraModelRepository.findById(modelId)
                    .orElseThrow(() -> new CustomException(ErrorCode.MODEL_NOT_FOUND));

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

            BigDecimal guidanceScaleDecimal = guidanceScale != null
                    ? BigDecimal.valueOf(guidanceScale)
                    : null;

            BigDecimal loraScaleDecimal = loraScale != null
                    ? BigDecimal.valueOf(loraScale)
                    : null;

            history = GenerationHistory.builder()
                    .model(model)
                    .user(user)
                    .prompt(prompt)
                    .negativePrompt(negativePrompt)
                    .steps(steps)
                    .guidanceScale(guidanceScaleDecimal)
                    .loraScale(loraScaleDecimal)
                    .seed(seed)
                    .numImages(numImages != null ? numImages : 1)
                    .status("SUCCESS")
                    .build();

            history = generationHistoryRepository.save(history);
        }

        // GeneratedImage 엔티티 생성 및 저장 (중복 방지)
        if (imageS3Keys != null && !imageS3Keys.isEmpty()) {
            // ✅ 이미 이미지가 저장되어 있으면 스킵 (중복 방지)
            List<GeneratedImage> existingImages = generatedImageRepository.findByGenerationHistoryOrderByDisplayOrder(history);
            if (!existingImages.isEmpty()) {
                log.info("⏭️ [중복 방지] 이미 이미지가 저장되어 있습니다: historyId={}, count={}", history.getId(), existingImages.size());
            } else {
                // 이미지가 없을 때만 저장
                for (int i = 0; i < imageS3Keys.size(); i++) {
                    String s3Key = imageS3Keys.get(i);

                    // S3 URL 생성 (생성 이미지 버킷 사용)
                    String s3Url = s3UploadService.generateImageDownloadUrl(s3Key);

                    GeneratedImage image = GeneratedImage.builder()
                            .generationHistory(history)
                            .s3Url(s3Url)
                            .s3Key(s3Key)
                            .displayOrder(i + 1)
                            .build();

                    generatedImageRepository.save(image);
                    history.addGeneratedImage(image);
                }
            }
        }

        log.info("Generation completed: historyId={}, images={}", history.getId(), imageS3Keys != null ? imageS3Keys.size() : 0);

        return GenerationHistoryResponse.from(history);
    }

    /**
     * 이미지 생성 실패 처리 (FastAPI 콜백)
     */
    @Transactional
    public void failGeneration(Long historyId, String errorMessage) {
        GenerationHistory history = generationHistoryRepository.findById(historyId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));

        history.markAsFailed(errorMessage);

        log.warn("Generation failed: historyId={}, error={}", historyId, errorMessage);
    }

    /**
     * 진행률 업데이트
     */
    @Transactional
    public void updateProgress(Long historyId, Integer currentStep, Integer totalSteps) {
        GenerationHistory history = generationHistoryRepository.findById(historyId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));

        history.updateProgress(currentStep, totalSteps);

        log.debug("Progress updated: historyId={}, {}/{}", historyId, currentStep, totalSteps);
    }

    /**
     * saveGenerationHistory (deprecated - completeGeneration 사용)
     */
    @Deprecated
    public GenerationHistoryResponse saveGenerationHistory(
            Long modelId,
            Long userId,
            String prompt,
            String negativePrompt,
            Integer steps,
            Double guidanceScale,
            Long seed,
            Integer numImages,
            List<String> imageS3Keys
    ) {
        return completeGeneration(null, modelId, userId, prompt, negativePrompt,
                steps, guidanceScale, null, seed, numImages, imageS3Keys);
    }

    /**
     * 사용자의 진행 중인 생성 작업 확인
     *
     * @param userId 사용자 ID
     * @return 진행 중인 작업이 있으면 해당 GenerationHistory, 없으면 null
     */
    public GenerationHistoryResponse getOngoingGeneration(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        return generationHistoryRepository.findFirstByUserAndStatusOrderByCreatedAtDesc(user, "GENERATING")
                .map(GenerationHistoryResponse::from)
                .orElse(null);
    }

    /**
     * 이미지 생성 요청 시작 (GenerationHistory 생성)
     *
     * Controller에서 FastAPI 호출 전에 먼저 DB에 기록합니다.
     * @return 생성된 GenerationHistory ID와 S3 키
     */
    @Transactional
    public GenerationStartResponse startGeneration(GenerateImageRequest request, Long userId) {
        // 진행 중인 작업이 있는지 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        generationHistoryRepository.findFirstByUserAndStatusOrderByCreatedAtDesc(user, "GENERATING")
                .ifPresent(history -> {
                    throw new CustomException(ErrorCode.INVALID_INPUT_VALUE,
                        "이미 진행 중인 이미지 생성 작업이 있습니다. (History ID: " + history.getId() + ")");
                });
        // 모델 존재 여부 확인
        LoraModel model = loraModelRepository.findById(request.getModelId())
                .orElseThrow(() -> new CustomException(ErrorCode.MODEL_NOT_FOUND));

        // S3 키가 없으면 에러 (학습 완료되지 않은 모델)
        if (model.getS3Key() == null || model.getS3Key().isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "모델 파일이 아직 생성되지 않았습니다.");
        }

        // BigDecimal 변환
        BigDecimal guidanceScaleDecimal = request.getGuidanceScale() != null
                ? BigDecimal.valueOf(request.getGuidanceScale())
                : null;

        BigDecimal loraScaleDecimal = request.getLoraScale() != null
                ? BigDecimal.valueOf(request.getLoraScale())
                : BigDecimal.valueOf(1.0);  // 기본값 1.0

        // GenerationHistory 생성 (GENERATING 상태)
        GenerationHistory history = GenerationHistory.builder()
                .model(model)
                .user(user)
                .prompt(request.getPrompt())
                .negativePrompt(request.getNegativePrompt())
                .steps(request.getSteps())
                .guidanceScale(guidanceScaleDecimal)
                .loraScale(loraScaleDecimal)
                .seed(request.getSeed() != null ? request.getSeed().longValue() : null)
                .numImages(request.getNumImages() != null ? request.getNumImages() : 1)
                .status("GENERATING")
                .build();

        GenerationHistory savedHistory = generationHistoryRepository.save(history);

        return new GenerationStartResponse(savedHistory.getId(), model.getS3Key());
    }

    /**
     * 이미지 생성 요청 검증 (deprecated - startGeneration 사용)
     *
     * @deprecated startGeneration() 사용
     */
    @Deprecated
    public String validateGenerationRequest(GenerateImageRequest request, Long userId) {
        return startGeneration(request, userId).getS3Key();
    }

    // 내부 DTO
    @Getter
    @AllArgsConstructor
    public static class GenerationStartResponse {
        private Long historyId;
        private String s3Key;
    }

    /**
     * 생성 기록 단건 조회
     */
    public GenerationHistoryResponse getGenerationHistory(Long historyId) {
        GenerationHistory history = generationHistoryRepository.findById(historyId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));

        return GenerationHistoryResponse.from(history);
    }

    /**
     * 사용자의 생성 기록 조회 (페이징)
     */
    public PageResponse<GenerationHistoryResponse> getUserGenerationHistory(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Page<GenerationHistory> historyPage = generationHistoryRepository.findByUser(user, pageable);

        List<GenerationHistoryResponse> responses = historyPage.getContent().stream()
                .map(GenerationHistoryResponse::from)
                .collect(Collectors.toList());

        return PageResponse.of(historyPage, responses);
    }

    /**
     * 사용자의 생성 기록 조회 (페이징, 모델 필터링 옵션)
     * - modelId가 null이면 모든 생성 기록 반환
     * - modelId가 있으면 해당 모델로 생성한 기록만 반환
     */
    public PageResponse<GenerationHistoryResponse> getUserGenerationHistory(Long userId, Long modelId, Pageable pageable) {
        if (modelId == null) {
            // 모델 필터링 없이 모든 생성 기록 반환
            return getUserGenerationHistory(userId, pageable);
        } else {
            // 특정 모델로 필터링
            return getUserModelGenerationHistory(userId, modelId, pageable);
        }
    }

    /**
     * 모델의 생성 기록 조회 (페이징)
     */
    public PageResponse<GenerationHistoryResponse> getModelGenerationHistory(Long modelId, Pageable pageable) {
        LoraModel model = loraModelRepository.findById(modelId)
                .orElseThrow(() -> new CustomException(ErrorCode.MODEL_NOT_FOUND));

        Page<GenerationHistory> historyPage = generationHistoryRepository.findByModel(model, pageable);

        List<GenerationHistoryResponse> responses = historyPage.getContent().stream()
                .map(GenerationHistoryResponse::from)
                .collect(Collectors.toList());

        return PageResponse.of(historyPage, responses);
    }

    /**
     * 사용자의 특정 모델 생성 기록 조회 (페이징)
     */
    public PageResponse<GenerationHistoryResponse> getUserModelGenerationHistory(
            Long userId,
            Long modelId,
            Pageable pageable
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        LoraModel model = loraModelRepository.findById(modelId)
                .orElseThrow(() -> new CustomException(ErrorCode.MODEL_NOT_FOUND));

        Page<GenerationHistory> historyPage = generationHistoryRepository.findByUserAndModel(user, model, pageable);

        List<GenerationHistoryResponse> responses = historyPage.getContent().stream()
                .map(GenerationHistoryResponse::from)
                .collect(Collectors.toList());

        return PageResponse.of(historyPage, responses);
    }


    /**
     * 생성 기록 삭제
     * - 샘플로 사용 중인 이미지가 있으면 삭제 불가
     * - S3에서 이미지도 함께 삭제
     */
    @Transactional
    public void deleteGenerationHistory(Long historyId, Long userId) {
        GenerationHistory history = generationHistoryRepository.findById(historyId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));

        // 권한 확인: 생성한 사용자만 삭제 가능
        if (!history.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        // 샘플로 사용 중인 이미지가 있는지 확인
        List<Long> generatedImageIds = history.getGeneratedImages().stream()
                .map(GeneratedImage::getId)
                .collect(Collectors.toList());

        boolean hasSampleImages = generatedImageIds.stream()
                .anyMatch(imageId -> modelSampleRepository.existsByGeneratedImageId(imageId));

        if (hasSampleImages) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE,
                    "샘플로 사용 중인 이미지가 있어 삭제할 수 없습니다. 먼저 샘플 선택을 해제해주세요.");
        }

        // S3에서 이미지 삭제
        List<String> s3Keys = history.getGeneratedImages().stream()
                .map(GeneratedImage::getS3Key)
                .filter(s3Key -> s3Key != null && !s3Key.isEmpty())
                .collect(Collectors.toList());

        // DB에서 먼저 삭제 (Cascade로 GeneratedImage도 함께 삭제됨)
        generationHistoryRepository.delete(history);

        // S3에서 이미지 삭제 (DB 삭제 후 실행하여 트랜잭션 롤백 시 S3도 복구 가능)
        for (String s3Key : s3Keys) {
            try {
                s3Service.deleteFile(S3Service.BucketType.GENERATED_IMAGES, s3Key);
                log.info("Deleted S3 image: {}", s3Key);
            } catch (Exception e) {
                log.error("Failed to delete S3 image: {}, error: {}", s3Key, e.getMessage());
                // S3 삭제 실패는 로그만 남기고 진행 (이미 DB에서는 삭제됨)
            }
        }
    }

    /**
     * 모델의 생성 이미지 조회 (소유자만)
     * - 모델 소유자가 샘플 선택을 위해 자신이 생성한 이미지들을 조회
     */
    public List<GeneratedImage> getModelGenerationImages(Long modelId, Long userId) {
        // 모델 소유자 확인 (테스트 유저 100은 모든 모델 접근 가능)
        LoraModel model = loraModelRepository.findById(modelId)
                .orElseThrow(() -> new CustomException(ErrorCode.MODEL_NOT_FOUND));

        if (!model.getUser().getId().equals(userId) && !userId.equals(100L)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        // 모델과 연관된 모든 생성 이미지 조회 (모델 소유자의 이미지들)
        return generatedImageRepository.findByModelIdAndUserId(modelId, model.getUser().getId());
    }

    /**
     * 사용자가 이미지 생성에 사용한 모델 목록 조회
     * - 생성 히스토리 필터링 드롭다운용
     * - 중복 제거, 최근 사용 순 정렬
     */
    public List<AvailableModelResponse> getAvailableModels(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        return generationHistoryRepository.findAvailableModelsByUser(user);
    }

}
