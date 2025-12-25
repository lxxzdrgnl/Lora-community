package rheon.wsd_lora_community.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 학습/생성 작업 강제 실패 처리 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class JobFailRequest {

    @NotBlank(message = "실패 사유는 필수입니다.")
    private String errorMessage;
}
