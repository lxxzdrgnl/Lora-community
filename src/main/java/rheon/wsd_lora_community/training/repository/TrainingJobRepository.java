package rheon.wsd_lora_community.training.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rheon.wsd_lora_community.model.entity.LoraModel;
import rheon.wsd_lora_community.training.entity.TrainingJob;
import rheon.wsd_lora_community.user.entity.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TrainingJobRepository extends JpaRepository<TrainingJob, Long> {

    Optional<TrainingJob> findByModel(LoraModel model);

    List<TrainingJob> findByUserOrderByCreatedAtDesc(User user);

    List<TrainingJob> findByStatus(TrainingJob.TrainingStatus status);

    Optional<TrainingJob> findTopByModelOrderByCreatedAtDesc(LoraModel model);

    /**
     * 사용자의 진행 중인 학습 작업 조회 (PENDING, IN_PROGRESS)
     */
    Optional<TrainingJob> findTopByUserAndStatusInOrderByCreatedAtDesc(
            User user, List<TrainingJob.TrainingStatus> statuses);

    /**
     * 특정 상태로 특정 시간 이전에 업데이트된 작업 조회 (복구용)
     */
    List<TrainingJob> findByStatusAndUpdatedAtBefore(TrainingJob.TrainingStatus status, LocalDateTime updatedAt);
}
