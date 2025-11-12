package rheon.wsd_lora_community.training.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rheon.wsd_lora_community.model.entity.LoraModel;
import rheon.wsd_lora_community.training.entity.TrainingJob;
import rheon.wsd_lora_community.user.entity.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrainingJobRepository extends JpaRepository<TrainingJob, Long> {

    Optional<TrainingJob> findByModel(LoraModel model);

    List<TrainingJob> findByUser(User user);

    List<TrainingJob> findByStatus(TrainingJob.TrainingStatus status);

    Optional<TrainingJob> findTopByModelOrderByCreatedAtDesc(LoraModel model);
}
