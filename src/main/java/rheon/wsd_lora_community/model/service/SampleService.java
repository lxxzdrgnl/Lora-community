package rheon.wsd_lora_community.model.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rheon.wsd_lora_community.global.exception.CustomException;
import rheon.wsd_lora_community.global.exception.ErrorCode;
import rheon.wsd_lora_community.model.dto.ModelSampleResponse;
import rheon.wsd_lora_community.model.dto.SampleCreateRequest;
import rheon.wsd_lora_community.model.entity.LoraModel;
import rheon.wsd_lora_community.model.entity.ModelSample;
import rheon.wsd_lora_community.model.repository.LoraModelRepository;
import rheon.wsd_lora_community.model.repository.ModelSampleRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 모델 샘플 이미지 관리 서비스
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SampleService {

    private final ModelSampleRepository modelSampleRepository;
    private final LoraModelRepository loraModelRepository;

    /**
     * 모델의 모든 샘플 이미지 조회
     */
    public List<ModelSampleResponse> getSamplesByModel(Long modelId) {
        LoraModel model = loraModelRepository.findById(modelId)
                .orElseThrow(() -> new CustomException(ErrorCode.MODEL_NOT_FOUND));

        return modelSampleRepository.findByModelOrderByDisplayOrderAsc(model).stream()
                .map(ModelSampleResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 샘플 이미지 단건 조회
     */
    public ModelSampleResponse getSample(Long sampleId) {
        ModelSample sample = modelSampleRepository.findById(sampleId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));

        return ModelSampleResponse.from(sample);
    }

    /**
     * 샘플 이미지 추가
     */
    @Transactional
    public ModelSampleResponse createSample(Long modelId, Long userId, SampleCreateRequest request) {
        LoraModel model = loraModelRepository.findById(modelId)
                .orElseThrow(() -> new CustomException(ErrorCode.MODEL_NOT_FOUND));

        // 권한 확인: 모델 소유자만 샘플 추가 가능
        if (!model.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        // 대표 이미지로 설정하는 경우, 기존 대표 이미지 해제
        if (Boolean.TRUE.equals(request.getIsPrimary())) {
            modelSampleRepository.findByModelAndIsPrimaryTrue(model)
                    .ifPresent(ModelSample::unsetPrimary);
        }

        // BigDecimal 변환
        BigDecimal guidanceScale = request.getGuidanceScale() != null
                ? BigDecimal.valueOf(request.getGuidanceScale())
                : null;

        ModelSample sample = ModelSample.builder()
                .model(model)
                .imageUrl(request.getImageUrl())
                .prompt(request.getPrompt())
                .negativePrompt(request.getNegativePrompt())
                .steps(request.getSteps())
                .guidanceScale(guidanceScale)
                .seed(request.getSeed())
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .isPrimary(request.getIsPrimary() != null ? request.getIsPrimary() : false)
                .build();

        ModelSample saved = modelSampleRepository.save(sample);
        return ModelSampleResponse.from(saved);
    }

    /**
     * 샘플 이미지 삭제
     */
    @Transactional
    public void deleteSample(Long sampleId, Long userId) {
        ModelSample sample = modelSampleRepository.findById(sampleId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));

        // 권한 확인: 모델 소유자만 삭제 가능
        if (!sample.getModel().getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        modelSampleRepository.delete(sample);
    }

    /**
     * 대표 이미지 설정
     */
    @Transactional
    public ModelSampleResponse setPrimarySample(Long sampleId, Long userId) {
        ModelSample sample = modelSampleRepository.findById(sampleId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));

        // 권한 확인
        if (!sample.getModel().getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        // 기존 대표 이미지 해제
        modelSampleRepository.findByModelAndIsPrimaryTrue(sample.getModel())
                .ifPresent(ModelSample::unsetPrimary);

        // 새 대표 이미지 설정
        sample.setPrimary();

        return ModelSampleResponse.from(sample);
    }

    /**
     * 샘플 이미지 표시 순서 변경
     */
    @Transactional
    public ModelSampleResponse updateDisplayOrder(Long sampleId, Integer displayOrder, Long userId) {
        ModelSample sample = modelSampleRepository.findById(sampleId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));

        // 권한 확인
        if (!sample.getModel().getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        sample.updateDisplayOrder(displayOrder);

        return ModelSampleResponse.from(sample);
    }

    /**
     * 샘플 이미지 프롬프트 정보 수정
     */
    @Transactional
    public ModelSampleResponse updateSamplePrompt(Long sampleId, String prompt, String negativePrompt, Long userId) {
        ModelSample sample = modelSampleRepository.findById(sampleId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));

        // 권한 확인
        if (!sample.getModel().getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        sample.updatePrompt(prompt, negativePrompt);

        return ModelSampleResponse.from(sample);
    }
}
