package rheon.wsd_lora_community.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "프롬프트 예시 생성 요청")
public class PromptCreateRequest {

    @NotBlank(message = "제목은 필수입니다")
    @Size(max = 100, message = "제목은 100자 이하여야 합니다")
    @Schema(description = "프롬프트 제목", example = "기본 프롬프트")
    private String title;

    @NotBlank(message = "프롬프트는 필수입니다")
    @Size(max = 5000, message = "프롬프트는 5000자 이하여야 합니다")
    @Schema(description = "프롬프트", example = "1girl, black hair, long hair, red eyes, anime style")
    private String prompt;

    @Size(max = 5000, message = "네거티브 프롬프트는 5000자 이하여야 합니다")
    @Schema(description = "네거티브 프롬프트", example = "nsfw, lowres, bad anatomy, bad hands")
    private String negativePrompt;

    @Size(max = 1000, message = "설명은 1000자 이하여야 합니다")
    @Schema(description = "프롬프트 설명", example = "기본적인 캐릭터 생성 프롬프트입니다.")
    private String description;

    @Schema(description = "표시 순서", example = "0")
    private Integer displayOrder;
}
