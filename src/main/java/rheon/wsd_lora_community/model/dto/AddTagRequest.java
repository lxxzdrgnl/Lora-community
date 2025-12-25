package rheon.wsd_lora_community.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 모델에 태그 추가 요청 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "모델에 태그 추가 요청")
public class AddTagRequest {

    @Schema(description = "태그 이름", example = "anime", required = true)
    @NotBlank(message = "태그 이름은 필수입니다.")
    private String tagName;
}
