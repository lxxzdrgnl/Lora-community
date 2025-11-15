package rheon.wsd_lora_community.generation.entity;

import jakarta.persistence.*;
import lombok.*;
import rheon.wsd_lora_community.global.dto.BaseEntity;
import rheon.wsd_lora_community.model.entity.LoraModel;
import rheon.wsd_lora_community.user.entity.User;

import java.math.BigDecimal;

/**
 * 이미지 생성 기록 엔티티
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

    @Column(nullable = false, length = 500)
    private String imageUrl;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isSample = false;

    // 비즈니스 메서드
    public void markAsSample() {
        this.isSample = true;
    }

    public void unmarkAsSample() {
        this.isSample = false;
    }
}
