package rheon.wsd_lora_community.community.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rheon.wsd_lora_community.community.entity.ModelFavorite;
import rheon.wsd_lora_community.community.repository.ModelFavoriteRepository;
import rheon.wsd_lora_community.global.dto.PageResponse;
import rheon.wsd_lora_community.global.exception.CustomException;
import rheon.wsd_lora_community.global.exception.ErrorCode;
import rheon.wsd_lora_community.model.dto.LoraModelResponse;
import rheon.wsd_lora_community.model.entity.LoraModel;
import rheon.wsd_lora_community.model.repository.LoraModelRepository;
import rheon.wsd_lora_community.user.entity.User;
import rheon.wsd_lora_community.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 모델 즐겨찾기 관리 서비스
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class FavoriteService {

    private final ModelFavoriteRepository modelFavoriteRepository;
    private final LoraModelRepository loraModelRepository;
    private final UserRepository userRepository;

    /**
     * 모델 즐겨찾기 토글
     * @return true: 즐겨찾기 추가, false: 즐겨찾기 취소
     */
    @Transactional
    public boolean toggleModelFavorite(Long modelId, Long userId) {
        LoraModel model = loraModelRepository.findById(modelId)
                .orElseThrow(() -> new CustomException(ErrorCode.MODEL_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 이미 즐겨찾기한 경우 취소
        if (modelFavoriteRepository.existsByModelAndUser(model, user)) {
            modelFavoriteRepository.deleteByModelAndUser(model, user);
            model.decrementFavoriteCount();
            return false; // 즐겨찾기 취소
        }

        // 즐겨찾기 추가
        ModelFavorite modelFavorite = ModelFavorite.builder()
                .model(model)
                .user(user)
                .build();
        modelFavoriteRepository.save(modelFavorite);
        model.incrementFavoriteCount();
        return true; // 즐겨찾기 추가
    }

    /**
     * 사용자가 특정 모델을 즐겨찾기했는지 확인
     */
    public boolean isModelFavoritedByUser(Long modelId, Long userId) {
        return modelFavoriteRepository.existsByModelIdAndUserId(modelId, userId);
    }

    /**
     * 사용자의 즐겨찾기 모델 목록 조회 (페이징)
     */
    public PageResponse<LoraModelResponse> getUserFavoriteModels(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Page<ModelFavorite> favoritesPage = modelFavoriteRepository.findByUser(user, pageable);

        List<LoraModelResponse> responses = favoritesPage.getContent().stream()
                .map(favorite -> LoraModelResponse.from(favorite.getModel()))
                .collect(Collectors.toList());

        return PageResponse.of(favoritesPage, responses);
    }

    /**
     * 모델의 즐겨찾기 수 조회
     */
    public Integer getModelFavoriteCount(Long modelId) {
        LoraModel model = loraModelRepository.findById(modelId)
                .orElseThrow(() -> new CustomException(ErrorCode.MODEL_NOT_FOUND));

        return model.getFavoriteCount();
    }
}
