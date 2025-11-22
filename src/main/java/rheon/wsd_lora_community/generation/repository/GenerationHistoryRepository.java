package rheon.wsd_lora_community.generation.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rheon.wsd_lora_community.generation.entity.GenerationHistory;
import rheon.wsd_lora_community.model.entity.LoraModel;
import rheon.wsd_lora_community.user.entity.User;

import java.util.Optional;

@Repository
public interface GenerationHistoryRepository extends JpaRepository<GenerationHistory, Long> {

    Page<GenerationHistory> findByUser(User user, Pageable pageable);

    Page<GenerationHistory> findByModel(LoraModel model, Pageable pageable);

    Page<GenerationHistory> findByUserAndModel(User user, LoraModel model, Pageable pageable);

    /**
     * 사용자의 진행 중인 생성 작업 조회 (GENERATING 상태)
     */
    Optional<GenerationHistory> findFirstByUserAndStatusOrderByCreatedAtDesc(User user, String status);
}
