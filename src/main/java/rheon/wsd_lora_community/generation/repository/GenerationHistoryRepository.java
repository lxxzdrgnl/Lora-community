package rheon.wsd_lora_community.generation.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rheon.wsd_lora_community.generation.entity.GenerationHistory;
import rheon.wsd_lora_community.model.entity.LoraModel;
import rheon.wsd_lora_community.user.entity.User;

@Repository
public interface GenerationHistoryRepository extends JpaRepository<GenerationHistory, Long> {

    Page<GenerationHistory> findByUser(User user, Pageable pageable);

    Page<GenerationHistory> findByModel(LoraModel model, Pageable pageable);

    Page<GenerationHistory> findByUserAndModel(User user, LoraModel model, Pageable pageable);
}
