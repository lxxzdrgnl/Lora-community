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
@Schema(description = "모델 샘플 이미지 추가 요청")
public class SampleCreateRequest {

    @NotBlank(message = "이미지 URL은 필수입니다")
    @Size(max = 500, message = "이미지 URL은 500자 이하여야 합니다")
    @Schema(description = "이미지 URL", example = "https://example.com/sample.png")
    private String imageUrl;

    @Size(max = 5000, message = "프롬프트는 5000자 이하여야 합니다")
    @Schema(description = "생성 프롬프트", example = "1girl, black hair, long hair, red eyes")
    private String prompt;

    @Size(max = 5000, message = "네거티브 프롬프트는 5000자 이하여야 합니다")
    @Schema(description = "네거티브 프롬프트", example = "nsfw, lowres, bad anatomy")
    private String negativePrompt;

    @Schema(description = "생성 스텝 수", example = "40")
    private Integer steps;

    @Schema(description = "가이던스 스케일", example = "7.5")
    private Double guidanceScale;

    @Schema(description = "시드 값", example = "123456")
    private Long seed;

    @Schema(description = "표시 순서", example = "0")
    private Integer displayOrder;

    @Schema(description = "대표 이미지 여부", example = "false")
    private Boolean isPrimary;
}
