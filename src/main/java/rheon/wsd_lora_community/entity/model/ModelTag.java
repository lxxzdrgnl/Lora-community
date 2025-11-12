package rheon.wsd_lora_community.entity.model;

import jakarta.persistence.*;
import lombok.*;
import rheon.wsd_lora_community.entity.common.BaseEntity;

/**
 * 모델-태그 연결 엔티티
 */
@Entity
@Table(name = "model_tags",
        uniqueConstraints = {
                @UniqueConstraint(name = "unique_model_tag", columnNames = {"model_id", "tag_id"})
        },
        indexes = {
                @Index(name = "idx_model", columnList = "model_id"),
                @Index(name = "idx_tag", columnList = "tag_id")
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ModelTag extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_id", nullable = false)
    private LoraModel model;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", nullable = false)
    private Tag tag;
}
