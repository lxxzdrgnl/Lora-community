package rheon.wsd_lora_community.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Google ID Token 인증 요청 DTO
 * - 모바일 앱에서 Google OAuth로 받은 ID Token 전달용
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GoogleTokenRequest {

    @NotBlank(message = "ID Token is required")
    private String idToken;
}
