package rheon.wsd_lora_community.generation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import rheon.wsd_lora_community.generation.entity.GeneratedImage;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "생성된 이미지 응답")
public class GeneratedImageResponse {

    @Schema(description = "이미지 ID", example = "1")
    private Long id;

    @Schema(description = "생성 기록 ID", example = "1")
    private Long generationHistoryId;

    @Schema(description = "S3 URL", example = "https://lora-generated-image-bucket.s3.ap-southeast-2.amazonaws.com/user-123/image.png")
    private String s3Url;

    @Schema(description = "S3 키", example = "user-123/image.png")
    private String s3Key;

    @Schema(description = "파일 크기 (바이트)", example = "1024000")
    private Long fileSize;

    @Schema(description = "표시 순서", example = "1")
    private Integer displayOrder;

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

    @Schema(description = "생성일", example = "2025-01-18T10:00:00")
    private LocalDateTime createdAt;

    public static GeneratedImageResponse from(GeneratedImage image) {
        return GeneratedImageResponse.builder()
                .id(image.getId())
                .generationHistoryId(image.getGenerationHistory().getId())
                .s3Url(image.getS3Url())
                .s3Key(image.getS3Key())
                .fileSize(image.getFileSize())
                .displayOrder(image.getDisplayOrder())
                .prompt(image.getGenerationHistory().getPrompt())
                .negativePrompt(image.getGenerationHistory().getNegativePrompt())
                .steps(image.getGenerationHistory().getSteps())
                .guidanceScale(image.getGenerationHistory().getGuidanceScale())
                .seed(image.getGenerationHistory().getSeed())
                .loraScale(image.getGenerationHistory().getLoraScale())
                .createdAt(image.getCreatedAt())
                .build();
    }
}
