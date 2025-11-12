package rheon.wsd_lora_community.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rheon.wsd_lora_community.entity.model.LoraModel;
import rheon.wsd_lora_community.entity.model.ModelSample;

import java.util.List;
import java.util.Optional;

@Repository
public interface ModelSampleRepository extends JpaRepository<ModelSample, Long> {

    List<ModelSample> findByModelOrderByDisplayOrderAsc(LoraModel model);

    Optional<ModelSample> findByModelAndIsPrimaryTrue(LoraModel model);

    long countByModel(LoraModel model);
}
