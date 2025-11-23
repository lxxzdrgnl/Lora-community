package rheon.wsd_lora_community.generation.entity;

import jakarta.persistence.*;
import lombok.*;
import rheon.wsd_lora_community.global.dto.BaseEntity;
import rheon.wsd_lora_community.model.entity.LoraModel;
import rheon.wsd_lora_community.user.entity.User;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 이미지 생성 기록 엔티티
 * - 한 번의 생성 요청 정보를 저장 (프롬프트, 파라미터 등)
 * - 실제 생성된 이미지들은 GeneratedImage 엔티티에 저장 (1:N 관계)
 */
@Entity
@Table(name = "generation_history", indexes = {
        @Index(name = "idx_generation_model", columnList = "model_id"),
        @Index(name = "idx_generation_user", columnList = "user_id"),
        @Index(name = "idx_generation_created", columnList = "created_at")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class GenerationHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_id", nullable = false)
    private LoraModel model;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String prompt;

    @Column(columnDefinition = "TEXT")
    private String negativePrompt;

    @Column
    private Integer steps;

    @Column(precision = 4, scale = 2)
    private BigDecimal guidanceScale;

    @Column
    private Long seed;

    /**
     * 생성된 이미지 수
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer numImages = 1;

    /**
     * 생성 상태 (GENERATING, SUCCESS, FAILED)
     */
    @Column(length = 20, nullable = false)
    @Builder.Default
    private String status = "GENERATING";

    /**
     * 현재 진행 스텝
     */
    @Column
    private Integer currentStep;

    /**
     * 전체 스텝 수
     */
    @Column
    private Integer totalSteps;

    /**
     * 에러 메시지 (실패 시)
     */
    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    /**
     * 생성된 이미지 목록 (1:N 관계)
     * - CascadeType.ALL: GenerationHistory 삭제 시 연관된 이미지도 함께 삭제
     * - orphanRemoval: 관계가 끊어진 이미지 자동 삭제
     */
    @OneToMany(mappedBy = "generationHistory", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<GeneratedImage> generatedImages = new ArrayList<>();

    // 비즈니스 메서드
    public void addGeneratedImage(GeneratedImage image) {
        this.generatedImages.add(image);
    }

    public void removeGeneratedImage(GeneratedImage image) {
        this.generatedImages.remove(image);
    }

    public void updateProgress(Integer currentStep, Integer totalSteps) {
        this.currentStep = currentStep;
        this.totalSteps = totalSteps;
    }

    public void markAsSuccess() {
        this.status = "SUCCESS";
        this.errorMessage = null;
    }

    public void markAsFailed(String errorMessage) {
        this.status = "FAILED";
        this.errorMessage = errorMessage;
    }
}
