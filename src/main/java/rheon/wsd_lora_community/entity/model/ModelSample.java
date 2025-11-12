package rheon.wsd_lora_community.entity.model;

import jakarta.persistence.*;
import lombok.*;
import rheon.wsd_lora_community.entity.common.BaseEntity;

import java.math.BigDecimal;

/**
 * 모델 샘플 이미지 엔티티
 */
@Entity
@Table(name = "model_samples", indexes = {
        @Index(name = "idx_model", columnList = "model_id"),
        @Index(name = "idx_primary", columnList = "model_id, is_primary")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ModelSample extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_id", nullable = false)
    private LoraModel model;

    @Column(nullable = false, length = 500)
    private String imageUrl;

    @Column(columnDefinition = "TEXT")
    private String prompt;

    @Column(columnDefinition = "TEXT")
    private String negativePrompt;

    @Column
    private Integer steps;

    @Column(precision = 4, scale = 2)
    private BigDecimal guidanceScale;

    @Column
    private Long seed;

    @Column
    @Builder.Default
    private Integer displayOrder = 0;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isPrimary = false;

    // 비즈니스 메서드
    public void updateDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public void setPrimary() {
        this.isPrimary = true;
    }

    public void unsetPrimary() {
        this.isPrimary = false;
    }

    public void updatePrompt(String prompt, String negativePrompt) {
        if (prompt != null) this.prompt = prompt;
        if (negativePrompt != null) this.negativePrompt = negativePrompt;
    }
}
