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

    @Schema(description = "생성 이미지 ID", example = "123")
    private Long generatedImageId;

    @Schema(description = "이미지 URL", example = "https://...")
    private String imageUrl;

    @Schema(description = "프롬프트")
    private String prompt;

    @Schema(description = "네거티브 프롬프트")
    private String negativePrompt;

    @Schema(description = "스텝 수", example = "30")
    private Integer steps;

    @Schema(description = "가이던스 스케일", example = "7.5")
    private BigDecimal guidanceScale;

    @Schema(description = "시드", example = "123456")
    private Long seed;

    @Schema(description = "LoRA 스케일", example = "1.0")
    private BigDecimal loraScale;

    @Schema(description = "표시 순서", example = "1")
    private Integer displayOrder;

    @Schema(description = "대표 이미지 여부", example = "true")
    private Boolean isPrimary;

    @Schema(description = "생성일", example = "2025-01-18T10:00:00")
    private LocalDateTime createdAt;

    public static ModelSampleResponse from(ModelSample sample) {
        return ModelSampleResponse.builder()
                .id(sample.getId())
                .generatedImageId(sample.getGeneratedImage().getId())
                .imageUrl(sample.getGeneratedImage().getS3Url())
                .prompt(sample.getGeneratedImage().getGenerationHistory().getPrompt())
                .negativePrompt(sample.getGeneratedImage().getGenerationHistory().getNegativePrompt())
                .steps(sample.getGeneratedImage().getGenerationHistory().getSteps())
                .guidanceScale(sample.getGeneratedImage().getGenerationHistory().getGuidanceScale())
                .seed(sample.getGeneratedImage().getGenerationHistory().getSeed())
                .loraScale(sample.getGeneratedImage().getGenerationHistory().getLoraScale())
                .displayOrder(sample.getDisplayOrder())
                .isPrimary(sample.getIsPrimary())
                .createdAt(sample.getCreatedAt())
                .build();
    }
}
