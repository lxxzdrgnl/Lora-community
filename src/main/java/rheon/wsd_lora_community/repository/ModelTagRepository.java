package rheon.wsd_lora_community.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rheon.wsd_lora_community.entity.model.LoraModel;
import rheon.wsd_lora_community.entity.model.ModelTag;
import rheon.wsd_lora_community.entity.model.Tag;

import java.util.List;
import java.util.Optional;

@Repository
public interface ModelTagRepository extends JpaRepository<ModelTag, Long> {

    List<ModelTag> findByModel(LoraModel model);

    Optional<ModelTag> findByModelAndTag(LoraModel model, Tag tag);

    boolean existsByModelAndTag(LoraModel model, Tag tag);

    void deleteByModel(LoraModel model);
}
