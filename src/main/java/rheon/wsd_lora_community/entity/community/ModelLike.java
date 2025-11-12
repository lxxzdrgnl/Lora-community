package rheon.wsd_lora_community.entity.community;

import jakarta.persistence.*;
import lombok.*;
import rheon.wsd_lora_community.entity.common.BaseEntity;
import rheon.wsd_lora_community.entity.model.LoraModel;
import rheon.wsd_lora_community.entity.user.User;

/**
 * 모델 좋아요 엔티티
 */
@Entity
@Table(name = "model_likes",
        uniqueConstraints = {
                @UniqueConstraint(name = "unique_model_like", columnNames = {"model_id", "user_id"})
        },
        indexes = {
                @Index(name = "idx_model", columnList = "model_id"),
                @Index(name = "idx_user", columnList = "user_id")
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ModelLike extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_id", nullable = false)
    private LoraModel model;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
