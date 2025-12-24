package rheon.wsd_lora_community.model.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rheon.wsd_lora_community.global.exception.CustomException;
import rheon.wsd_lora_community.global.exception.ErrorCode;
import rheon.wsd_lora_community.model.dto.TagResponse;
import rheon.wsd_lora_community.model.entity.LoraModel;
import rheon.wsd_lora_community.model.entity.ModelTag;
import rheon.wsd_lora_community.model.entity.Tag;
import rheon.wsd_lora_community.model.repository.LoraModelRepository;
import rheon.wsd_lora_community.model.repository.ModelTagRepository;
import rheon.wsd_lora_community.model.repository.TagRepository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 태그 관리 서비스
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;
    private final ModelTagRepository modelTagRepository;
    private final LoraModelRepository loraModelRepository;

    /**
     * 모든 태그 조회
     */
    public List<TagResponse> getAllTags() {
        return tagRepository.findAll().stream()
                .map(TagResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 인기 태그 조회 (Top 20)
     */
    public List<TagResponse> getPopularTags() {
        return tagRepository.findTop20ByOrderByUsageCountDesc().stream()
                .map(TagResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 태그 검색
     */
    public List<TagResponse> searchTags(String keyword) {
        return tagRepository.searchByName(keyword).stream()
                .map(TagResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 태그 단건 조회
     */
    public TagResponse getTag(Long tagId) {
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));

        return TagResponse.from(tag);
    }

    /**
     * 태그 생성 또는 조회 (존재하면 조회, 없으면 생성)
     */
    @Transactional
    public Tag getOrCreateTag(String tagName) {
        return tagRepository.findByName(tagName)
                .orElseGet(() -> {
                    Tag newTag = Tag.builder()
                            .name(tagName)
                            .usageCount(0)
                            .build();
                    return tagRepository.save(newTag);
                });
    }

    /**
     * 모델에 태그 추가
     */
    @Transactional
    public void addTagToModel(Long modelId, String tagName, Long userId) {
        LoraModel model = loraModelRepository.findById(modelId)
                .orElseThrow(() -> new CustomException(ErrorCode.MODEL_NOT_FOUND));

        // 권한 확인: 모델 소유자만 태그 추가 가능
        if (!model.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        // 태그 생성 또는 조회
        Tag tag = getOrCreateTag(tagName);

        // 이미 연결되어 있는지 확인
        if (modelTagRepository.existsByModelAndTag(model, tag)) {
            throw new CustomException(ErrorCode.DUPLICATE_RESOURCE);
        }

        // 모델-태그 연결
        ModelTag modelTag = ModelTag.builder()
                .model(model)
                .tag(tag)
                .build();
        modelTagRepository.save(modelTag);

        // 태그 사용 횟수 증가
        tag.incrementUsageCount();
    }

    /**
     * 모델에서 태그 제거
     */
    @Transactional
    public void removeTagFromModel(Long modelId, Long tagId, Long userId) {
        LoraModel model = loraModelRepository.findById(modelId)
                .orElseThrow(() -> new CustomException(ErrorCode.MODEL_NOT_FOUND));

        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));

        // 권한 확인
        if (!model.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        // 모델-태그 연결 찾기
        ModelTag modelTag = modelTagRepository.findByModelAndTag(model, tag)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));

        // 연결 삭제
        modelTagRepository.delete(modelTag);

        // 태그 사용 횟수 감소
        tag.decrementUsageCount();
    }

    /**
     * 모델의 모든 태그 조회
     */
    public List<TagResponse> getTagsByModel(Long modelId) {
        LoraModel model = loraModelRepository.findById(modelId)
                .orElseThrow(() -> new CustomException(ErrorCode.MODEL_NOT_FOUND));

        return modelTagRepository.findByModel(model).stream()
                .map(modelTag -> TagResponse.from(modelTag.getTag()))
                .collect(Collectors.toList());
    }
}
