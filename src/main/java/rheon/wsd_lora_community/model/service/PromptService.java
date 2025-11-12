package rheon.wsd_lora_community.model.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rheon.wsd_lora_community.global.exception.CustomException;
import rheon.wsd_lora_community.global.exception.ErrorCode;
import rheon.wsd_lora_community.model.dto.PromptCreateRequest;
import rheon.wsd_lora_community.model.dto.PromptResponse;
import rheon.wsd_lora_community.model.dto.PromptUpdateRequest;
import rheon.wsd_lora_community.model.entity.LoraModel;
import rheon.wsd_lora_community.model.entity.ModelPrompt;
import rheon.wsd_lora_community.model.repository.LoraModelRepository;
import rheon.wsd_lora_community.model.repository.ModelPromptRepository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 프롬프트 예시 관리 서비스
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PromptService {

    private final ModelPromptRepository modelPromptRepository;
    private final LoraModelRepository loraModelRepository;

    /**
     * 모델의 모든 프롬프트 예시 조회
     */
    public List<PromptResponse> getPromptsByModel(Long modelId) {
        LoraModel model = loraModelRepository.findById(modelId)
                .orElseThrow(() -> new CustomException(ErrorCode.MODEL_NOT_FOUND));

        return modelPromptRepository.findByModelOrderByDisplayOrderAsc(model).stream()
                .map(PromptResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 프롬프트 예시 단건 조회
     */
    public PromptResponse getPrompt(Long promptId) {
        ModelPrompt prompt = modelPromptRepository.findById(promptId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));

        return PromptResponse.from(prompt);
    }

    /**
     * 프롬프트 예시 생성
     */
    @Transactional
    public PromptResponse createPrompt(Long modelId, Long userId, PromptCreateRequest request) {
        LoraModel model = loraModelRepository.findById(modelId)
                .orElseThrow(() -> new CustomException(ErrorCode.MODEL_NOT_FOUND));

        // 권한 확인: 모델 소유자만 프롬프트 추가 가능
        if (!model.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        ModelPrompt prompt = ModelPrompt.builder()
                .model(model)
                .title(request.getTitle())
                .prompt(request.getPrompt())
                .negativePrompt(request.getNegativePrompt())
                .description(request.getDescription())
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .build();

        ModelPrompt saved = modelPromptRepository.save(prompt);
        return PromptResponse.from(saved);
    }

    /**
     * 프롬프트 예시 수정
     */
    @Transactional
    public PromptResponse updatePrompt(Long promptId, Long userId, PromptUpdateRequest request) {
        ModelPrompt prompt = modelPromptRepository.findById(promptId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));

        // 권한 확인: 모델 소유자만 수정 가능
        if (!prompt.getModel().getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        // 업데이트
        prompt.update(
                request.getTitle(),
                request.getPrompt(),
                request.getNegativePrompt(),
                request.getDescription()
        );

        // displayOrder 별도 처리
        if (request.getDisplayOrder() != null) {
            prompt.updateDisplayOrder(request.getDisplayOrder());
        }

        return PromptResponse.from(prompt);
    }

    /**
     * 프롬프트 예시 삭제
     */
    @Transactional
    public void deletePrompt(Long promptId, Long userId) {
        ModelPrompt prompt = modelPromptRepository.findById(promptId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));

        // 권한 확인: 모델 소유자만 삭제 가능
        if (!prompt.getModel().getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        modelPromptRepository.delete(prompt);
    }

    /**
     * 프롬프트 예시 표시 순서 변경
     */
    @Transactional
    public PromptResponse updateDisplayOrder(Long promptId, Integer displayOrder, Long userId) {
        ModelPrompt prompt = modelPromptRepository.findById(promptId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));

        // 권한 확인
        if (!prompt.getModel().getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        prompt.updateDisplayOrder(displayOrder);

        return PromptResponse.from(prompt);
    }
}
