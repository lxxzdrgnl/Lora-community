package rheon.wsd_lora_community.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * JWT 토큰 응답 DTO
 * - Access Token, Refresh Token, 사용자 정보 포함
 */
@Getter
@AllArgsConstructor
public class TokenResponse {

    private String accessToken;
    private String refreshToken;
    private Long userId;
    private String email;
    private String nickname;
    private String profileImageUrl;
}
