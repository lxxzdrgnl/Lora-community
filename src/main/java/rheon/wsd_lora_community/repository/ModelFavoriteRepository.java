package rheon.wsd_lora_community.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rheon.wsd_lora_community.entity.community.ModelFavorite;
import rheon.wsd_lora_community.entity.model.LoraModel;
import rheon.wsd_lora_community.entity.user.User;

import java.util.Optional;

@Repository
public interface ModelFavoriteRepository extends JpaRepository<ModelFavorite, Long> {

    Optional<ModelFavorite> findByModelAndUser(LoraModel model, User user);

    boolean existsByModelAndUser(LoraModel model, User user);

    void deleteByModelAndUser(LoraModel model, User user);

    Page<ModelFavorite> findByUser(User user, Pageable pageable);
}
