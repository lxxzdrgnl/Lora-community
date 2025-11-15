package rheon.wsd_lora_community.community.entity;

import jakarta.persistence.*;
import lombok.*;
import rheon.wsd_lora_community.global.dto.BaseEntity;
import rheon.wsd_lora_community.model.entity.LoraModel;
import rheon.wsd_lora_community.user.entity.User;

import java.time.LocalDateTime;

/**
 * 댓글 엔티티
 */
@Entity
@Table(name = "comments", indexes = {
        @Index(name = "idx_comment_model", columnList = "model_id"),
        @Index(name = "idx_comment_user", columnList = "user_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Comment extends BaseEntity {

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
    private String content;

    @Column
    @Builder.Default
    private Integer likeCount = 0;

    @Column
    private LocalDateTime deletedAt;

    // 비즈니스 메서드
    public void updateContent(String content) {
        this.content = content;
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
}
