package rheon.wsd_lora_community.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rheon.wsd_lora_community.model.entity.LoraModel;
import rheon.wsd_lora_community.model.entity.ModelPrompt;

import java.util.List;

@Repository
public interface ModelPromptRepository extends JpaRepository<ModelPrompt, Long> {

    List<ModelPrompt> findByModelOrderByDisplayOrderAsc(LoraModel model);

    // modelId로 검색 (LoraModelService에서 사용)
    List<ModelPrompt> findByModelIdOrderByDisplayOrder(Long modelId);
}
