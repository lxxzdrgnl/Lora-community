package rheon.wsd_lora_community.model.entity;

import jakarta.persistence.*;
import lombok.*;
import rheon.wsd_lora_community.generation.entity.GeneratedImage;
import rheon.wsd_lora_community.global.dto.BaseEntity;

/**
 * 모델 샘플 이미지 엔티티
 * - 생성된 이미지 중 모델 소유자가 선택한 샘플 이미지
 * - GeneratedImage를 참조하여 실제 이미지와 생성 파라미터 정보 연결
 */
@Entity
@Table(name = "model_samples", indexes = {
        @Index(name = "idx_sample_model", columnList = "model_id"),
        @Index(name = "idx_sample_model_order", columnList = "model_id, display_order")
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

    /**
     * 생성된 이미지 참조
     * - GeneratedImage를 통해 s3Url, prompt, steps 등의 정보 접근
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "generated_image_id", nullable = false)
    private GeneratedImage generatedImage;

    /**
     * 표시 순서 (1부터 시작)
     */
    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private Integer displayOrder = 0;

    /**
     * 대표 이미지 여부 (첫 번째가 대표)
     */
    @Column(name = "is_primary", nullable = false)
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
}
