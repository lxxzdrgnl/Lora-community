package rheon.wsd_lora_community.model.entity;

import jakarta.persistence.*;
import lombok.*;
import rheon.wsd_lora_community.global.dto.BaseEntity;
import rheon.wsd_lora_community.user.entity.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * LoRA 모델 엔티티
 */
@Entity
@Table(name = "lora_models", indexes = {
        @Index(name = "idx_lora_model_user", columnList = "user_id"),
        @Index(name = "idx_lora_model_public_status", columnList = "is_public, status"),
        @Index(name = "idx_lora_model_created", columnList = "created_at")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class LoraModel extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 100)
    private String characterName;

    @Column(length = 100)
    private String style;

    @Column
    @Builder.Default
    private Integer trainingImagesCount = 0;

    @Column
    private Integer epochs;

    @Column(precision = 10, scale = 8)
    private BigDecimal learningRate;

    @Column
    private Integer loraRank;

    @Column(name = "s3_key", length = 500)
    private String s3Key;  // S3 저장 경로 (models/{userId}/{modelName}.safetensors)

    @Column(name = "file_size")
    private Long fileSize;  // 모델 파일 크기 (bytes)

    @Column(length = 200)
    @Builder.Default
    private String baseModel = "stablediffusionapi/anything-v5";

    @Column(nullable = false)
    @Builder.Default
    private Boolean isPublic = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ModelStatus status = ModelStatus.TRAINING;

    @Column
    @Builder.Default
    private Integer viewCount = 0;

    @Column
    @Builder.Default
    private Integer likeCount = 0;

    @Column
    private LocalDateTime deletedAt;

    // 연관관계
    @OneToMany(mappedBy = "model", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ModelTag> modelTags = new ArrayList<>();

    /**
     * 모델 상태
     */
    public enum ModelStatus {
        TRAINING,   // 학습 중
        COMPLETED,  // 학습 완료
        FAILED      // 학습 실패
    }

    // 비즈니스 메서드
    public void updateInfo(String title, String description, String characterName, String style) {
        if (title != null) this.title = title;
        if (description != null) this.description = description;
        if (characterName != null) this.characterName = characterName;
        if (style != null) this.style = style;
    }

    public void updateTitle(String title) {
        this.title = title;
    }

    public void updateDescription(String description) {
        this.description = description;
    }

    public void updateCharacterName(String characterName) {
        this.characterName = characterName;
    }

    public void updateStyle(String style) {
        this.style = style;
    }

    public void updateIsPublic(Boolean isPublic) {
        this.isPublic = isPublic;
    }

    public void updateTrainingInfo(Integer epochs, BigDecimal learningRate, Integer loraRank, Integer trainingImagesCount) {
        this.epochs = epochs;
        this.learningRate = learningRate;
        this.loraRank = loraRank;
        this.trainingImagesCount = trainingImagesCount;
    }

    public void completeTrainingWithS3(String s3Key, Long fileSize) {
        this.s3Key = s3Key;
        this.fileSize = fileSize;
        this.status = ModelStatus.COMPLETED;
    }

    public void failTraining() {
        this.status = ModelStatus.FAILED;
    }

    public void publish() {
        if (this.status != ModelStatus.COMPLETED) {
            throw new IllegalStateException("학습이 완료된 모델만 공개할 수 있습니다.");
        }
        this.isPublic = true;
    }

    public void unpublish() {
        this.isPublic = false;
    }

    public void incrementViewCount() {
        this.viewCount++;
    }

    public void incrementLikeCount() {
        this.likeCount++;
    }

    public void decrementLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public boolean isOwner(User user) {
        return this.user.getId().equals(user.getId());
    }

    public void updateStatus(ModelStatus status) {
        this.status = status;
    }
}
