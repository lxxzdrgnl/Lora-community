package rheon.wsd_lora_community.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rheon.wsd_lora_community.entity.model.LoraModel;
import rheon.wsd_lora_community.entity.model.ModelPrompt;

import java.util.List;

@Repository
public interface ModelPromptRepository extends JpaRepository<ModelPrompt, Long> {

    List<ModelPrompt> findByModelOrderByDisplayOrderAsc(LoraModel model);
}
