package rheon.wsd_lora_community.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rheon.wsd_lora_community.entity.community.ModelLike;
import rheon.wsd_lora_community.entity.model.LoraModel;
import rheon.wsd_lora_community.entity.user.User;

import java.util.Optional;

@Repository
public interface ModelLikeRepository extends JpaRepository<ModelLike, Long> {

    Optional<ModelLike> findByModelAndUser(LoraModel model, User user);

    boolean existsByModelAndUser(LoraModel model, User user);

    void deleteByModelAndUser(LoraModel model, User user);
}
