package rheon.wsd_lora_community.generation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "이미지 생성 요청")
public class GenerateImageRequest {

    @NotNull(message = "모델 ID는 필수입니다")
    @Schema(description = "LoRA 모델 ID", example = "1")
    private Long modelId;

    @NotBlank(message = "프롬프트는 필수입니다")
    @Size(max = 5000, message = "프롬프트는 5000자 이하여야 합니다")
    @Schema(description = "생성 프롬프트", example = "1girl, black hair, long hair, red eyes, anime style")
    private String prompt;

    @Size(max = 5000, message = "네거티브 프롬프트는 5000자 이하여야 합니다")
    @Schema(description = "네거티브 프롬프트", example = "nsfw, lowres, bad anatomy, bad hands")
    private String negativePrompt;

    @Min(value = 1, message = "스텝 수는 1 이상이어야 합니다")
    @Max(value = 150, message = "스텝 수는 150 이하여야 합니다")
    @Schema(description = "생성 스텝 수", example = "40")
    private Integer steps;

    @Min(value = 1, message = "가이던스 스케일은 1 이상이어야 합니다")
    @Max(value = 20, message = "가이던스 스케일은 20 이하여야 합니다")
    @Schema(description = "가이던스 스케일", example = "7.5")
    private Double guidanceScale;

    @Min(value = 1, message = "생성 이미지 수는 1 이상이어야 합니다")
    @Max(value = 10, message = "생성 이미지 수는 10 이하여야 합니다")
    @Schema(description = "생성할 이미지 수", example = "1")
    private Integer numImages;

    @Schema(description = "시드 값 (재현성을 위해)", example = "123456")
    private Long seed;

    @Min(value = 0, message = "LoRA 강도는 0 이상이어야 합니다")
    @Max(value = 2, message = "LoRA 강도는 2 이하여야 합니다")
    @Schema(description = "LoRA 강도 (0.0~2.0, 기본값 1.0)", example = "1.0")
    private Double loraScale;
}
