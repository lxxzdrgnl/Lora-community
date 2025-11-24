package rheon.wsd_lora_community.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "유저 프로필 수정 요청")
public class UserUpdateRequest {

    @Size(min = 2, max = 10, message = "닉네임은 2-10자 이하여야 합니다")
    @Schema(description = "닉네임", example = "새로운닉네임")
    private String nickname;

    @Size(max = 500, message = "프로필 이미지 URL은 500자 이하여야 합니다")
    @Schema(description = "프로필 이미지 URL", example = "https://example.com/profile.jpg")
    private String profileImageUrl;
}
