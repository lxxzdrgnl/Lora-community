package rheon.wsd_lora_community.community.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rheon.wsd_lora_community.community.entity.ModelLike;
import rheon.wsd_lora_community.community.repository.ModelLikeRepository;
import rheon.wsd_lora_community.global.dto.PageResponse;
import rheon.wsd_lora_community.global.exception.CustomException;
import rheon.wsd_lora_community.global.exception.ErrorCode;
import rheon.wsd_lora_community.model.dto.LoraModelResponse;
import rheon.wsd_lora_community.model.entity.LoraModel;
import rheon.wsd_lora_community.model.repository.LoraModelRepository;
import rheon.wsd_lora_community.model.repository.ModelSampleRepository;
import rheon.wsd_lora_community.user.entity.User;
import rheon.wsd_lora_community.user.repository.UserRepository;

/**
 * 모델 좋아요 관리 서비스
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class LikeService {

    private final ModelLikeRepository modelLikeRepository;
    private final LoraModelRepository loraModelRepository;
    private final UserRepository userRepository;
    private final ModelSampleRepository modelSampleRepository;

    /**
     * 모델 좋아요 토글
     * @return true: 좋아요 추가, false: 좋아요 취소
     */
    @Transactional
    public boolean toggleModelLike(Long modelId, Long userId) {
        LoraModel model = loraModelRepository.findById(modelId)
                .orElseThrow(() -> new CustomException(ErrorCode.MODEL_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 이미 좋아요한 경우 취소
        if (modelLikeRepository.existsByModelAndUser(model, user)) {
            modelLikeRepository.deleteByModelAndUser(model, user);
            model.decrementLikeCount();
            return false; // 좋아요 취소
        }

        // 좋아요 추가
        ModelLike modelLike = ModelLike.builder()
                .model(model)
                .user(user)
                .build();
        modelLikeRepository.save(modelLike);
        model.incrementLikeCount();
        return true; // 좋아요 추가
    }

    /**
     * 사용자가 특정 모델에 좋아요를 눌렀는지 확인
     */
    public boolean isModelLikedByUser(Long modelId, Long userId) {
        return modelLikeRepository.existsByModelIdAndUserId(modelId, userId);
    }

    /**
     * 모델의 좋아요 수 조회
     */
    public Integer getModelLikeCount(Long modelId) {
        LoraModel model = loraModelRepository.findById(modelId)
                .orElseThrow(() -> new CustomException(ErrorCode.MODEL_NOT_FOUND));

        return model.getLikeCount();
    }

    /**
     * 사용자가 좋아요한 모델 목록 조회
     */
    public PageResponse<LoraModelResponse> getUserLikedModels(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Page<ModelLike> likedModels = modelLikeRepository.findByUserOrderByCreatedAtDesc(user, pageable);

        Page<LoraModelResponse> modelResponses = likedModels.map(like -> {
            LoraModel model = like.getModel();
            String thumbnailUrl = getThumbnailUrl(model);
            return LoraModelResponse.from(model, true, thumbnailUrl); // isLiked는 항상 true
        });

        return PageResponse.of(modelResponses);
    }

    /**
     * 모델의 썸네일 URL 조회 (대표 샘플 이미지)
     */
    private String getThumbnailUrl(LoraModel model) {
        return modelSampleRepository.findByModelIdOrderByDisplayOrder(model.getId())
                .stream()
                .findFirst()
                .map(sample -> sample.getImageUrl())
                .orElse(null);
    }
}
