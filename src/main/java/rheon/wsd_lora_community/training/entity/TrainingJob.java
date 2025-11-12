package rheon.wsd_lora_community.training.entity;

import jakarta.persistence.*;
import lombok.*;
import rheon.wsd_lora_community.global.dto.BaseEntity;
import rheon.wsd_lora_community.model.entity.LoraModel;
import rheon.wsd_lora_community.user.entity.User;

import java.time.LocalDateTime;

/**
 * 학습 작업 엔티티
 */
@Entity
@Table(name = "training_jobs", indexes = {
        @Index(name = "idx_model", columnList = "model_id"),
        @Index(name = "idx_user", columnList = "user_id"),
        @Index(name = "idx_status", columnList = "status")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class TrainingJob extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_id", nullable = false)
    private LoraModel model;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TrainingStatus status = TrainingStatus.PENDING;

    @Column
    @Builder.Default
    private Integer currentEpoch = 0;

    @Column
    private Integer totalEpochs;

    @Column(length = 50)
    private String phase;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    @Column
    private LocalDateTime startedAt;

    @Column
    private LocalDateTime completedAt;

    /**
     * 학습 상태
     */
    public enum TrainingStatus {
        PENDING,        // 대기 중
        PREPROCESSING,  // 전처리 중
        TRAINING,       // 학습 중
        SUCCESS,        // 성공
        FAILED          // 실패
    }

    // 비즈니스 메서드
    public void start(Integer totalEpochs) {
        this.status = TrainingStatus.PREPROCESSING;
        this.totalEpochs = totalEpochs;
        this.startedAt = LocalDateTime.now();
    }

    public void updateProgress(Integer currentEpoch, String phase) {
        this.currentEpoch = currentEpoch;
        this.phase = phase;
        if (this.status == TrainingStatus.PREPROCESSING && phase != null && phase.equals("training")) {
            this.status = TrainingStatus.TRAINING;
        }
    }

    public void complete() {
        this.status = TrainingStatus.SUCCESS;
        this.completedAt = LocalDateTime.now();
    }

    public void fail(String errorMessage) {
        this.status = TrainingStatus.FAILED;
        this.errorMessage = errorMessage;
        this.completedAt = LocalDateTime.now();
    }

    public boolean isInProgress() {
        return status == TrainingStatus.PENDING ||
                status == TrainingStatus.PREPROCESSING ||
                status == TrainingStatus.TRAINING;
    }
}
