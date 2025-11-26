package rheon.wsd_lora_community.model.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rheon.wsd_lora_community.generation.entity.GeneratedImage;
import rheon.wsd_lora_community.generation.repository.GeneratedImageRepository;
import rheon.wsd_lora_community.global.exception.CustomException;
import rheon.wsd_lora_community.global.exception.ErrorCode;
import rheon.wsd_lora_community.model.dto.ModelSampleResponse;
import rheon.wsd_lora_community.model.entity.LoraModel;
import rheon.wsd_lora_community.model.entity.ModelSample;
import rheon.wsd_lora_community.model.repository.LoraModelRepository;
import rheon.wsd_lora_community.model.repository.ModelSampleRepository;

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
    private final GeneratedImageRepository generatedImageRepository;

    /**
     * 모델의 샘플 이미지 목록 조회
     */
    public List<ModelSampleResponse> getSamplesByModel(Long modelId) {
        return modelSampleRepository.findByModelIdOrderByDisplayOrder(modelId)
                .stream()
                .map(ModelSampleResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 생성 이미지를 샘플로 추가
     * - 모델 소유자만 가능
     */
    @Transactional
    public ModelSampleResponse addSample(Long modelId, Long generatedImageId, Long userId) {
        // 모델 조회 및 권한 확인
        LoraModel model = loraModelRepository.findById(modelId)
                .orElseThrow(() -> new CustomException(ErrorCode.MODEL_NOT_FOUND));

        if (!model.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        // 생성 이미지 조회
        GeneratedImage generatedImage = generatedImageRepository.findById(generatedImageId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));

        // 생성 이미지가 해당 모델로 생성된 것인지 확인
        if (!generatedImage.getGenerationHistory().getModel().getId().equals(modelId)) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE,
                    "이 이미지는 해당 모델로 생성되지 않았습니다.");
        }

        // 이미 샘플로 등록되어 있는지 확인
        if (modelSampleRepository.existsByModelIdAndGeneratedImageId(modelId, generatedImageId)) {
            throw new CustomException(ErrorCode.DUPLICATE_RESOURCE, "이미 샘플로 등록된 이미지입니다.");
        }

        // 현재 샘플 개수 조회 (다음 순서 계산용)
        long currentCount = modelSampleRepository.countByModelId(modelId);

        // 샘플 생성
        ModelSample sample = ModelSample.builder()
                .model(model)
                .generatedImage(generatedImage)
                .displayOrder((int) currentCount + 1)
                .isPrimary(currentCount == 0) // 첫 샘플이면 대표 이미지로 설정
                .build();

        ModelSample savedSample = modelSampleRepository.save(sample);

        return ModelSampleResponse.from(savedSample);
    }

    /**
     * 샘플 이미지 삭제
     * - 모델 소유자만 가능
     */
    @Transactional
    public void deleteSample(Long sampleId, Long userId) {
        ModelSample sample = modelSampleRepository.findById(sampleId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));

        // 권한 확인
        if (!sample.getModel().getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        // 대표 이미지였다면, 다른 샘플을 대표로 설정
        if (sample.getIsPrimary()) {
            List<ModelSample> samples = modelSampleRepository
                    .findByModelIdOrderByDisplayOrder(sample.getModel().getId());

            // 삭제할 샘플을 제외한 첫 번째 샘플을 대표로 설정
            samples.stream()
                    .filter(s -> !s.getId().equals(sampleId))
                    .findFirst()
                    .ifPresent(ModelSample::setPrimary);
        }

        modelSampleRepository.delete(sample);
    }

    /**
     * 대표 이미지 설정
     * - 모델 소유자만 가능
     */
    @Transactional
    public void setPrimarySample(Long sampleId, Long userId) {
        ModelSample sample = modelSampleRepository.findById(sampleId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));

        // 권한 확인
        if (!sample.getModel().getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        // 기존 대표 이미지 해제
        modelSampleRepository.findPrimarySampleByModelId(sample.getModel().getId())
                .ifPresent(ModelSample::unsetPrimary);

        // 새로운 대표 이미지 설정
        sample.setPrimary();
    }

    /**
     * 샘플 순서 변경
     * - 모델 소유자만 가능
     */
    @Transactional
    public void updateSampleOrder(Long modelId, List<Long> sampleIds, Long userId) {
        // 모델 조회 및 권한 확인
        LoraModel model = loraModelRepository.findById(modelId)
                .orElseThrow(() -> new CustomException(ErrorCode.MODEL_NOT_FOUND));

        if (!model.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        // 각 샘플의 순서 업데이트
        for (int i = 0; i < sampleIds.size(); i++) {
            Long sampleId = sampleIds.get(i);
            ModelSample sample = modelSampleRepository.findById(sampleId)
                    .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));

            // 해당 모델의 샘플인지 확인
            if (!sample.getModel().getId().equals(modelId)) {
                throw new CustomException(ErrorCode.INVALID_INPUT_VALUE,
                        "샘플 ID " + sampleId + "는 이 모델에 속하지 않습니다.");
            }

            sample.updateDisplayOrder(i + 1);
        }
    }
}
