package rheon.wsd_lora_community.generation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import rheon.wsd_lora_community.generation.entity.GenerationHistory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "이미지 생성 기록 응답")
public class GenerationHistoryResponse {

    @Schema(description = "생성 기록 ID", example = "1")
    private Long id;

    @Schema(description = "모델 ID", example = "1")
    private Long modelId;

    @Schema(description = "모델 제목", example = "소녀 전선 캐릭터 LoRA")
    private String modelTitle;

    @Schema(description = "유저 ID", example = "1")
    private Long userId;

    @Schema(description = "생성 프롬프트", example = "1girl, black hair, long hair, red eyes")
    private String prompt;

    @Schema(description = "네거티브 프롬프트", example = "nsfw, lowres, bad anatomy")
    private String negativePrompt;

    @Schema(description = "생성 스텝 수", example = "40")
    private Integer steps;

    @Schema(description = "가이던스 스케일", example = "7.5")
    private BigDecimal guidanceScale;

    @Schema(description = "시드 값", example = "123456")
    private Long seed;

    @Schema(description = "생성된 이미지 개수", example = "3")
    private Integer numImages;

    @Schema(description = "생성된 이미지 목록")
    private List<GeneratedImageResponse> generatedImages;

    @Schema(description = "생성일", example = "2025-01-13T10:00:00")
    private LocalDateTime createdAt;

    public static GenerationHistoryResponse from(GenerationHistory history) {
        return GenerationHistoryResponse.builder()
                .id(history.getId())
                .modelId(history.getModel().getId())
                .modelTitle(history.getModel().getTitle())
                .userId(history.getUser().getId())
                .prompt(history.getPrompt())
                .negativePrompt(history.getNegativePrompt())
                .steps(history.getSteps())
                .guidanceScale(history.getGuidanceScale())
                .seed(history.getSeed())
                .numImages(history.getNumImages())
                .generatedImages(history.getGeneratedImages().stream()
                        .map(GeneratedImageResponse::from)
                        .collect(Collectors.toList()))
                .createdAt(history.getCreatedAt())
                .build();
    }
}
