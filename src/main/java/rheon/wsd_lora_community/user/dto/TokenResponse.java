package rheon.wsd_lora_community.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * JWT 토큰 응답 DTO
 * - Access Token, Refresh Token, 사용자 정보 포함
 */
@Getter
@AllArgsConstructor
@Schema(description = "JWT 토큰 응답")
public class TokenResponse {

    @Schema(description = "Access Token (1시간 유효)", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String accessToken;

    @Schema(description = "Refresh Token (7일 유효)", example = "rt_9f8e7d6c5b4a3210fedcba0987654321")
    private String refreshToken;

    @Schema(description = "유저 ID", example = "12345")
    private Long userId;

    @Schema(description = "이메일", example = "user@example.com")
    private String email;

    @Schema(description = "닉네임", example = "LoRA마스터")
    private String nickname;

    @Schema(description = "프로필 이미지 URL", example = "https://lh3.googleusercontent.com/a/ACg8ocK...")
    private String profileImageUrl;
}
