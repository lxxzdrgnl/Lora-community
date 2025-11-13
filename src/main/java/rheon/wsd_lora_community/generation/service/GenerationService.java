package rheon.wsd_lora_community.generation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rheon.wsd_lora_community.generation.dto.GenerateImageRequest;
import rheon.wsd_lora_community.generation.dto.GenerationHistoryResponse;
import rheon.wsd_lora_community.generation.entity.GenerationHistory;
import rheon.wsd_lora_community.generation.repository.GenerationHistoryRepository;
import rheon.wsd_lora_community.global.dto.PageResponse;
import rheon.wsd_lora_community.global.exception.CustomException;
import rheon.wsd_lora_community.global.exception.ErrorCode;
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
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GenerationService {

    private final GenerationHistoryRepository generationHistoryRepository;
    private final LoraModelRepository loraModelRepository;
    private final UserRepository userRepository;

    /**
     * 이미지 생성 기록 저장
     *
     * 실제 이미지 생성 후 FastAPI에서 호출하여 기록을 저장합니다.
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
            String imageUrl
    ) {
        LoraModel model = loraModelRepository.findById(modelId)
                .orElseThrow(() -> new CustomException(ErrorCode.MODEL_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // BigDecimal 변환
        BigDecimal guidanceScaleDecimal = guidanceScale != null
                ? BigDecimal.valueOf(guidanceScale)
                : null;

        GenerationHistory history = GenerationHistory.builder()
                .model(model)
                .user(user)
                .prompt(prompt)
                .negativePrompt(negativePrompt)
                .steps(steps)
                .guidanceScale(guidanceScaleDecimal)
                .seed(seed)
                .imageUrl(imageUrl)
                .isSample(false)
                .build();

        GenerationHistory saved = generationHistoryRepository.save(history);

        return GenerationHistoryResponse.from(saved);
    }

    /**
     * 이미지 생성 요청 검증
     *
     * Controller에서 FastAPI 호출 전에 이 메서드로 검증합니다.
     * @return LoRA 모델 파일 경로
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

        // 모델 파일 경로 반환 (없으면 기본 경로)
        return model.getModelPath() != null ? model.getModelPath() : "my_lora_model";
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
     * 생성 기록을 샘플로 등록
     */
    @Transactional
    public GenerationHistoryResponse markAsSample(Long historyId, Long userId) {
        GenerationHistory history = generationHistoryRepository.findById(historyId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));

        // 권한 확인: 모델 소유자만 샘플로 등록 가능
        if (!history.getModel().getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        history.markAsSample();

        return GenerationHistoryResponse.from(history);
    }

    /**
     * 샘플 등록 취소
     */
    @Transactional
    public GenerationHistoryResponse unmarkAsSample(Long historyId, Long userId) {
        GenerationHistory history = generationHistoryRepository.findById(historyId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));

        // 권한 확인: 모델 소유자만 샘플 취소 가능
        if (!history.getModel().getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        history.unmarkAsSample();

        return GenerationHistoryResponse.from(history);
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
