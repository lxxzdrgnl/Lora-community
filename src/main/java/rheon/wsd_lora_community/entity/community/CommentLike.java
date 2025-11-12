package rheon.wsd_lora_community.entity.community;

import jakarta.persistence.*;
import lombok.*;
import rheon.wsd_lora_community.entity.common.BaseEntity;
import rheon.wsd_lora_community.entity.user.User;

/**
 * 댓글 좋아요 엔티티
 */
@Entity
@Table(name = "comment_likes",
        uniqueConstraints = {
                @UniqueConstraint(name = "unique_comment_like", columnNames = {"comment_id", "user_id"})
        },
        indexes = {
                @Index(name = "idx_comment", columnList = "comment_id"),
                @Index(name = "idx_user", columnList = "user_id")
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CommentLike extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id", nullable = false)
    private Comment comment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
