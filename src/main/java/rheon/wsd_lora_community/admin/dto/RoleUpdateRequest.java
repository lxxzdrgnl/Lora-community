package rheon.wsd_lora_community.admin.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 유저 역할 변경 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RoleUpdateRequest {

    @NotNull(message = "역할은 필수입니다.")
    private String role;  // "USER", "TEST", "ADMIN"
}
