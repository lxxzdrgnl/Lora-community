package rheon.wsd_lora_community.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import rheon.wsd_lora_community.model.entity.ModelSample;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "모델 샘플 이미지 응답")
public class ModelSampleResponse {

    @Schema(description = "샘플 ID", example = "1")
    private Long id;

    @Schema(description = "모델 ID", example = "1")
    private Long modelId;

    @Schema(description = "이미지 URL", example = "https://example.com/sample.png")
    private String imageUrl;

    @Schema(description = "생성 프롬프트", example = "1girl, black hair, long hair")
    private String prompt;

    @Schema(description = "네거티브 프롬프트", example = "nsfw, lowres")
    private String negativePrompt;

    @Schema(description = "생성 스텝 수", example = "40")
    private Integer steps;

    @Schema(description = "가이던스 스케일", example = "7.5")
    private BigDecimal guidanceScale;

    @Schema(description = "시드 값", example = "123456")
    private Long seed;

    @Schema(description = "표시 순서", example = "0")
    private Integer displayOrder;

    @Schema(description = "대표 이미지 여부", example = "true")
    private Boolean isPrimary;

    @Schema(description = "생성일", example = "2025-01-13T10:00:00")
    private LocalDateTime createdAt;

    public static ModelSampleResponse from(ModelSample sample) {
        return ModelSampleResponse.builder()
                .id(sample.getId())
                .modelId(sample.getModel().getId())
                .imageUrl(sample.getImageUrl())
                .prompt(sample.getPrompt())
                .negativePrompt(sample.getNegativePrompt())
                .steps(sample.getSteps())
                .guidanceScale(sample.getGuidanceScale())
                .seed(sample.getSeed())
                .displayOrder(sample.getDisplayOrder())
                .isPrimary(sample.getIsPrimary())
                .createdAt(sample.getCreatedAt())
                .build();
    }
}
