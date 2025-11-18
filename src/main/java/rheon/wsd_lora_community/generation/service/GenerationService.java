package rheon.wsd_lora_community.generation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rheon.wsd_lora_community.generation.dto.GenerateImageRequest;
import rheon.wsd_lora_community.generation.dto.GenerationHistoryResponse;
import rheon.wsd_lora_community.generation.entity.GeneratedImage;
import rheon.wsd_lora_community.generation.entity.GenerationHistory;
import rheon.wsd_lora_community.generation.repository.GeneratedImageRepository;
import rheon.wsd_lora_community.generation.repository.GenerationHistoryRepository;
import rheon.wsd_lora_community.global.dto.PageResponse;
import rheon.wsd_lora_community.global.exception.CustomException;
import rheon.wsd_lora_community.global.exception.ErrorCode;
import rheon.wsd_lora_community.global.service.S3UploadService;
import rheon.wsd_lora_community.model.entity.LoraModel;
import rheon.wsd_lora_community.model.repository.LoraModelRepository;
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
    private final LoraModelRepository loraModelRepository;
    private final UserRepository userRepository;
    private final S3UploadService s3UploadService;

    /**
     * 이미지 생성 기록 저장 (FastAPI에서 호출)
     *
     * @param modelId 모델 ID
     * @param userId 사용자 ID
     * @param prompt 프롬프트
     * @param negativePrompt 네거티브 프롬프트
     * @param steps 스텝 수
     * @param guidanceScale 가이던스 스케일
     * @param seed 시드
     * @param numImages 생성된 이미지 개수
     * @param imageS3Keys S3에 저장된 이미지 키 리스트 (FastAPI가 업로드 후 전달)
     * @return 생성 기록 응답
     */
    @Transactional
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
        LoraModel model = loraModelRepository.findById(modelId)
                .orElseThrow(() -> new CustomException(ErrorCode.MODEL_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // BigDecimal 변환
        BigDecimal guidanceScaleDecimal = guidanceScale != null
                ? BigDecimal.valueOf(guidanceScale)
                : null;

        // GenerationHistory 생성
        GenerationHistory history = GenerationHistory.builder()
                .model(model)
                .user(user)
                .prompt(prompt)
                .negativePrompt(negativePrompt)
                .steps(steps)
                .guidanceScale(guidanceScaleDecimal)
                .seed(seed)
                .numImages(numImages != null ? numImages : 1)
                .build();

        GenerationHistory savedHistory = generationHistoryRepository.save(history);

        // GeneratedImage 엔티티 생성 및 저장
        if (imageS3Keys != null && !imageS3Keys.isEmpty()) {
            for (int i = 0; i < imageS3Keys.size(); i++) {
                String s3Key = imageS3Keys.get(i);

                // S3 URL 생성
                String s3Url = s3UploadService.generateDownloadPresignedUrl(s3Key);

                GeneratedImage image = GeneratedImage.builder()
                        .generationHistory(savedHistory)
                        .s3Url(s3Url)
                        .s3Key(s3Key)
                        .displayOrder(i + 1)
                        .isSample(false)
                        .build();

                generatedImageRepository.save(image);
                savedHistory.addGeneratedImage(image);
            }
        }

        log.info("Saved generation history: historyId={}, images={}", savedHistory.getId(), imageS3Keys.size());

        return GenerationHistoryResponse.from(savedHistory);
    }

    /**
     * 이미지 생성 요청 검증
     *
     * Controller에서 FastAPI 호출 전에 이 메서드로 검증합니다.
     * @return LoRA 모델의 S3 키
     */
    public String validateGenerationRequest(GenerateImageRequest request, Long userId) {
        // 모델 존재 여부 확인
        LoraModel model = loraModelRepository.findById(request.getModelId())
                .orElseThrow(() -> new CustomException(ErrorCode.MODEL_NOT_FOUND));

        // 모델이 COMPLETED 상태인지 확인
        if (model.getStatus() != LoraModel.ModelStatus.COMPLETED) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        // 사용자 존재 여부 확인
        userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // S3 키가 없으면 에러
        if (model.getS3Key() == null || model.getS3Key().isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        // S3 키 반환
        return model.getS3Key();
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
     * 생성된 이미지를 샘플로 등록
     */
    @Transactional
    public void markImageAsSample(Long imageId, Long userId) {
        GeneratedImage image = generatedImageRepository.findById(imageId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));

        // 권한 확인: 모델 소유자만 샘플로 등록 가능
        if (!image.getGenerationHistory().getModel().getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        image.markAsSample();
    }

    /**
     * 샘플 등록 취소
     */
    @Transactional
    public void unmarkImageAsSample(Long imageId, Long userId) {
        GeneratedImage image = generatedImageRepository.findById(imageId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));

        // 권한 확인: 모델 소유자만 샘플 취소 가능
        if (!image.getGenerationHistory().getModel().getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        image.unmarkAsSample();
    }

    /**
     * 생성 기록 삭제
     */
    @Transactional
    public void deleteGenerationHistory(Long historyId, Long userId) {
        GenerationHistory history = generationHistoryRepository.findById(historyId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));

        // 권한 확인: 생성한 사용자만 삭제 가능
        if (!history.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        generationHistoryRepository.delete(history);
    }
}
