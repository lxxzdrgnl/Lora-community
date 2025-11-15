package rheon.wsd_lora_community.community.entity;

import jakarta.persistence.*;
import lombok.*;
import rheon.wsd_lora_community.global.dto.BaseEntity;
import rheon.wsd_lora_community.model.entity.LoraModel;
import rheon.wsd_lora_community.user.entity.User;

/**
 * 모델 즐겨찾기 엔티티
 */
@Entity
@Table(name = "model_favorites",
        uniqueConstraints = {
                @UniqueConstraint(name = "unique_model_favorite", columnNames = {"model_id", "user_id"})
        },
        indexes = {
                @Index(name = "idx_model_favorite_model", columnList = "model_id"),
                @Index(name = "idx_model_favorite_user", columnList = "user_id")
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ModelFavorite extends BaseEntity {

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
