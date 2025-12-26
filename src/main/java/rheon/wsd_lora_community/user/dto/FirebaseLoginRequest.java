package rheon.wsd_lora_community.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Firebase ID Token 인증 요청 DTO
 * - 앱/웹에서 Firebase Auth로 받은 ID Token 전달용
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FirebaseLoginRequest {

    @NotBlank(message = "Firebase ID Token is required")
    private String idToken;
}
