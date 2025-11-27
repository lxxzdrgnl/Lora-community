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
        @Index(name = "idx_training_model", columnList = "model_id"),
        @Index(name = "idx_training_user", columnList = "user_id"),
        @Index(name = "idx_training_status", columnList = "status")
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
    @JoinColumn(name = "model_id")
    private LoraModel model;  // nullable - 학습 완료 후 생성

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 학습 파라미터 (학습 시작 시 저장)
    @Column(nullable = false, length = 200)
    private String modelName;

    @Column(columnDefinition = "TEXT")
    private String modelDescription;

    @Column
    private Integer trainingImagesCount;

    @Column
    private Integer epochs;

    @Column
    private Double learningRate;

    @Column
    private Integer loraRank;

    @Column(length = 200)
    private String baseModel;

    @Column(length = 100)
    private String triggerWord;

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

    // 학습 완료 후 모델 연결
    public void linkModel(LoraModel model) {
        this.model = model;
    }

    // 학습 파라미터 설정
    public void setTrainingParams(String modelName, String modelDescription, Integer trainingImagesCount,
                                  Integer epochs, Double learningRate, Integer loraRank,
                                  String baseModel, String triggerWord) {
        this.modelName = modelName;
        this.modelDescription = modelDescription;
        this.trainingImagesCount = trainingImagesCount;
        this.epochs = epochs;
        this.learningRate = learningRate;
        this.loraRank = loraRank;
        this.baseModel = baseModel;
        this.triggerWord = triggerWord;
    }
}
