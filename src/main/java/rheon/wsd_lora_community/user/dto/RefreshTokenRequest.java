package rheon.wsd_lora_community.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Refresh Token 요청 DTO (토큰 갱신, 로그아웃)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Refresh Token 요청")
public class RefreshTokenRequest {

    @Schema(description = "Refresh Token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...", required = true)
    @NotBlank(message = "Refresh Token은 필수입니다.")
    private String refreshToken;
}
