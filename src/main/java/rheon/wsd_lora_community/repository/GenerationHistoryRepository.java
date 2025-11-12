package rheon.wsd_lora_community.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rheon.wsd_lora_community.entity.generation.GenerationHistory;
import rheon.wsd_lora_community.entity.model.LoraModel;
import rheon.wsd_lora_community.entity.user.User;

@Repository
public interface GenerationHistoryRepository extends JpaRepository<GenerationHistory, Long> {

    Page<GenerationHistory> findByUser(User user, Pageable pageable);

    Page<GenerationHistory> findByModel(LoraModel model, Pageable pageable);

    Page<GenerationHistory> findByUserAndModel(User user, LoraModel model, Pageable pageable);
}
