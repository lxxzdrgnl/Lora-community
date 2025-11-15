package rheon.wsd_lora_community.model.entity;

import jakarta.persistence.*;
import lombok.*;
import rheon.wsd_lora_community.global.dto.BaseEntity;

/**
 * 모델 프롬프트 예시 엔티티
 */
@Entity
@Table(name = "model_prompts", indexes = {
        @Index(name = "idx_prompt_model", columnList = "model_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ModelPrompt extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_id", nullable = false)
    private LoraModel model;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String prompt;

    @Column(columnDefinition = "TEXT")
    private String negativePrompt;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column
    @Builder.Default
    private Integer displayOrder = 0;

    // 비즈니스 메서드
    public void update(String title, String prompt, String negativePrompt, String description) {
        if (title != null) this.title = title;
        if (prompt != null) this.prompt = prompt;
        if (negativePrompt != null) this.negativePrompt = negativePrompt;
        if (description != null) this.description = description;
    }

    public void updateDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }
}
