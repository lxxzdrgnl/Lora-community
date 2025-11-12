package rheon.wsd_lora_community.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rheon.wsd_lora_community.model.entity.LoraModel;
import rheon.wsd_lora_community.model.entity.ModelSample;

import java.util.List;
import java.util.Optional;

@Repository
public interface ModelSampleRepository extends JpaRepository<ModelSample, Long> {

    List<ModelSample> findByModelOrderByDisplayOrderAsc(LoraModel model);

    List<ModelSample> findByModelIdOrderByDisplayOrder(Long modelId);

    Optional<ModelSample> findByModelAndIsPrimaryTrue(LoraModel model);

    long countByModel(LoraModel model);
}
