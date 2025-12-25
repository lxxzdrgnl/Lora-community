package rheon.wsd_lora_community.model.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rheon.wsd_lora_community.global.exception.CustomException;
import rheon.wsd_lora_community.global.exception.ErrorCode;
import rheon.wsd_lora_community.global.service.S3UploadService;
import rheon.wsd_lora_community.model.dto.*;
import rheon.wsd_lora_community.model.entity.LoraModel;
import rheon.wsd_lora_community.model.entity.Tag;
import rheon.wsd_lora_community.model.repository.*;
import rheon.wsd_lora_community.community.repository.ModelLikeRepository;
import rheon.wsd_lora_community.user.entity.User;
import rheon.wsd_lora_community.user.service.UserService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LoraModelService {

    private final LoraModelRepository loraModelRepository;
    private final ModelSampleRepository modelSampleRepository;
    private final ModelPromptRepository modelPromptRepository;
    private final ModelTagRepository modelTagRepository;
    private final TagRepository tagRepository;
    private final ModelLikeRepository modelLikeRepository;
    private final UserService userService;
    private final S3UploadService s3UploadService;

    /**
     * 모델 생성
     * @deprecated 더 이상 사용하지 않음 - TrainingService.handleTrainingSuccess에서 자동 생성
     */
    @Deprecated
    @Transactional
    public LoraModelResponse createModel(Long userId, ModelCreateRequest request) {
        User user = userService.findUserEntityById(userId);

        LoraModel model = LoraModel.builder()
                .user(user)
                .title(request.getTitle())
                .description(request.getDescription())
                .isPublic(request.getIsPublic() != null ? request.getIsPublic() : false)
                .build();

        LoraModel savedModel = loraModelRepository.save(model);
        return LoraModelResponse.from(savedModel);
    }

    /**
     * 모델 목록 조회 (공개 모델, 페이징)
     */
    public Page<LoraModelResponse> getPublicModels(Long currentUserId, Pageable pageable) {
        Page<LoraModel> models = loraModelRepository.findByIsPublicTrueAndDeletedAtIsNull(pageable);
        return models.map(model -> {
            Boolean isLiked = currentUserId != null &&
                modelLikeRepository.existsByModelIdAndUserId(model.getId(), currentUserId);
            String thumbnailUrl = getThumbnailUrl(model);
            return LoraModelResponse.from(model, isLiked, thumbnailUrl);
        });
    }

    /**
     * 유저별 모델 목록 조회
     * - 학습 완료된 모델만 표시됨 (학습 실패 시 모델이 생성되지 않음)
     */
    public Page<LoraModelResponse> getModelsByUser(Long userId, Pageable pageable) {
        Page<LoraModel> models = loraModelRepository.findByUserIdExcludingFailedOrderByCreatedAtDesc(userId, pageable);
        return models.map(model -> {
            String thumbnailUrl = getThumbnailUrl(model);
            return LoraModelResponse.from(model, null, thumbnailUrl);
        });
    }

    /**
     * 모델 상세 조회
     */
    @Transactional
    public LoraModelDetailResponse getModelDetail(Long modelId, Long currentUserId) {
        LoraModel model = loraModelRepository.findById(modelId)
                .orElseThrow(() -> new CustomException(ErrorCode.MODEL_NOT_FOUND));

        // 조회수 증가
        model.incrementViewCount();

        // 샘플 이미지
        List<ModelSampleResponse> samples = modelSampleRepository.findByModelIdOrderByDisplayOrder(modelId)
                .stream()
                .map(ModelSampleResponse::from)
                .collect(Collectors.toList());

        // 프롬프트
        List<PromptResponse> prompts = modelPromptRepository.findByModelIdOrderByDisplayOrder(modelId)
                .stream()
                .map(PromptResponse::from)
                .collect(Collectors.toList());

        // 태그
        List<TagResponse> tags = modelTagRepository.findByModelId(modelId)
                .stream()
                .map(mt -> mt.getTag())
                .map(TagResponse::from)
                .collect(Collectors.toList());

        // 현재 유저의 좋아요/즐겨찾기 여부
        Boolean isLiked = currentUserId != null &&
                modelLikeRepository.existsByModelIdAndUserId(modelId, currentUserId);

        return LoraModelDetailResponse.from(model, samples, prompts, tags, isLiked);
    }

    /**
     * 모델 수정
     */
    @Transactional
    public LoraModelResponse updateModel(Long modelId, Long userId, ModelUpdateRequest request) {
        LoraModel model = loraModelRepository.findById(modelId)
                .orElseThrow(() -> new CustomException(ErrorCode.MODEL_NOT_FOUND));

        // 권한 확인
        if (!model.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        // 모델 정보 업데이트
        if (request.getTitle() != null) {
            model.updateTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            model.updateDescription(request.getDescription());
        }
        if (request.getIsPublic() != null) {
            model.updateIsPublic(request.getIsPublic());
        }

        return LoraModelResponse.from(model);
    }

    /**
     * 모델 삭제 (소프트 삭제)
     */
    @Transactional
    public void deleteModel(Long modelId, Long userId) {
        LoraModel model = loraModelRepository.findById(modelId)
                .orElseThrow(() -> new CustomException(ErrorCode.MODEL_NOT_FOUND));

        // 권한 확인: 작성자 또는 ADMIN만 삭제 가능
        if (!model.getUser().getId().equals(userId) && !rheon.wsd_lora_community.global.util.AuthenticationUtil.isAdmin()) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        model.softDelete();
    }

    /**
     * 태그로 모델 검색
     */
    public Page<LoraModelResponse> searchModelsByTags(List<String> tagNames, Long currentUserId, Pageable pageable) {
        List<Tag> tags = tagRepository.findByNameIn(tagNames);
        List<Long> tagIds = tags.stream().map(Tag::getId).collect(Collectors.toList());

        Page<LoraModel> models = loraModelRepository.findPublicModelsByTags(tagIds, pageable);
        return models.map(model -> {
            Boolean isLiked = currentUserId != null &&
                modelLikeRepository.existsByModelIdAndUserId(model.getId(), currentUserId);
            String thumbnailUrl = getThumbnailUrl(model);
            return LoraModelResponse.from(model, isLiked, thumbnailUrl);
        });
    }

    /**
     * 제목/설명으로 모델 검색
     */
    public Page<LoraModelResponse> searchModels(String query, Long currentUserId, Pageable pageable) {
        Page<LoraModel> models = loraModelRepository.searchByTitleOrDescription(query, pageable);
        return models.map(model -> {
            Boolean isLiked = currentUserId != null &&
                modelLikeRepository.existsByModelIdAndUserId(model.getId(), currentUserId);
            String thumbnailUrl = getThumbnailUrl(model);
            return LoraModelResponse.from(model, isLiked, thumbnailUrl);
        });
    }

    /**
     * 인기 모델 조회 (좋아요 순)
     */
    public Page<LoraModelResponse> getPopularModels(Long currentUserId, Pageable pageable) {
        Page<LoraModel> models = loraModelRepository.findByIsPublicTrueAndDeletedAtIsNullOrderByLikeCountDesc(pageable);
        return models.map(model -> {
            Boolean isLiked = currentUserId != null &&
                modelLikeRepository.existsByModelIdAndUserId(model.getId(), currentUserId);
            String thumbnailUrl = getThumbnailUrl(model);
            return LoraModelResponse.from(model, isLiked, thumbnailUrl);
        });
    }

    /**
     * 모델 엔티티 조회 (내부용)
     */
    public LoraModel findModelEntityById(Long modelId) {
        return loraModelRepository.findById(modelId)
                .orElseThrow(() -> new CustomException(ErrorCode.MODEL_NOT_FOUND));
    }

    /**
     * 모델의 썸네일 URL 조회 (대표 샘플 이미지)
     */
    private String getThumbnailUrl(LoraModel model) {
        return modelSampleRepository.findByModelIdOrderByDisplayOrder(model.getId())
                .stream()
                .findFirst()
                .map(sample -> sample.getGeneratedImage().getS3Url())
                .orElse(null);
    }
}
