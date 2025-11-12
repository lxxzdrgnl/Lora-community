package rheon.wsd_lora_community.user.entity;

import jakarta.persistence.*;
import lombok.*;
import rheon.wsd_lora_community.global.dto.BaseEntity;

import java.time.LocalDateTime;

/**
 * 유저 엔티티
 */
@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_email", columnList = "email"),
        @Index(name = "idx_oauth", columnList = "oauth_provider, oauth_provider_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 50)
    private String nickname;

    @Column(length = 500)
    private String profileImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OAuthProvider oauthProvider;

    @Column(nullable = false, unique = true)
    private String oauthProviderId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Role role = Role.USER;

    @Column
    private LocalDateTime deletedAt;

    /**
     * OAuth 제공자
     */
    public enum OAuthProvider {
        GOOGLE
    }

    /**
     * 사용자 역할
     */
    public enum Role {
        USER, ADMIN
    }

    // 비즈니스 메서드
    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void updateProfileImage(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public void softDelete() {
        this.deletedAt = java.time.LocalDateTime.now();
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }
}
