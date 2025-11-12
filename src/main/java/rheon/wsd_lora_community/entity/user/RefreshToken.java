package rheon.wsd_lora_community.entity.user;

import jakarta.persistence.*;
import lombok.*;
import rheon.wsd_lora_community.entity.common.BaseEntity;

import java.time.LocalDateTime;

/**
 * 리프레시 토큰 엔티티
 */
@Entity
@Table(name = "refresh_tokens", indexes = {
        @Index(name = "idx_user", columnList = "user_id"),
        @Index(name = "idx_token", columnList = "token")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class RefreshToken extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, unique = true, length = 500)
    private String token;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    // 비즈니스 메서드
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public void updateToken(String token, LocalDateTime expiresAt) {
        this.token = token;
        this.expiresAt = expiresAt;
    }
}
