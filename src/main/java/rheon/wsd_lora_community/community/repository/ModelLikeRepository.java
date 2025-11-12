package rheon.wsd_lora_community.community.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rheon.wsd_lora_community.community.entity.ModelLike;
import rheon.wsd_lora_community.model.entity.LoraModel;
import rheon.wsd_lora_community.user.entity.User;

import java.util.Optional;

@Repository
public interface ModelLikeRepository extends JpaRepository<ModelLike, Long> {

    Optional<ModelLike> findByModelAndUser(LoraModel model, User user);

    boolean existsByModelAndUser(LoraModel model, User user);

    void deleteByModelAndUser(LoraModel model, User user);

    // modelId와 userId로 존재 여부 확인 (LoraModelService에서 사용)
    boolean existsByModelIdAndUserId(Long modelId, Long userId);
}
