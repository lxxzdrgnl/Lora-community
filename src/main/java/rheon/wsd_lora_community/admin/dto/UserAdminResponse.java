package rheon.wsd_lora_community.admin.dto;

import lombok.Builder;
import lombok.Getter;
import rheon.wsd_lora_community.user.entity.User;

import java.time.LocalDateTime;

/**
 * 관리자용 유저 응답 DTO
 */
@Getter
@Builder
public class UserAdminResponse {
    private Long id;
    private String email;
    private String name;
    private String nickname;
    private String profileImageUrl;
    private String oauthProvider;
    private String role;
    private LocalDateTime createdAt;
    private LocalDateTime deletedAt;

    public static UserAdminResponse from(User user) {
        return UserAdminResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .nickname(user.getNickname())
                .profileImageUrl(user.getProfileImageUrl())
                .oauthProvider(user.getOauthProvider().name())
                .role(user.getRole().name())
                .createdAt(user.getCreatedAt())
                .deletedAt(user.getDeletedAt())
                .build();
    }
}
