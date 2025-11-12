package rheon.wsd_lora_community.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import rheon.wsd_lora_community.model.entity.ModelPrompt;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "프롬프트 예시 응답")
public class PromptResponse {

    @Schema(description = "프롬프트 ID", example = "1")
    private Long id;

    @Schema(description = "모델 ID", example = "1")
    private Long modelId;

    @Schema(description = "제목", example = "기본 프롬프트")
    private String title;

    @Schema(description = "프롬프트", example = "1girl, black hair, long hair, red eyes")
    private String prompt;

    @Schema(description = "네거티브 프롬프트", example = "nsfw, lowres, bad anatomy")
    private String negativePrompt;

    @Schema(description = "설명", example = "기본적인 캐릭터 생성 프롬프트입니다.")
    private String description;

    @Schema(description = "표시 순서", example = "0")
    private Integer displayOrder;

    @Schema(description = "생성일", example = "2025-01-13T10:00:00")
    private LocalDateTime createdAt;

    public static PromptResponse from(ModelPrompt prompt) {
        return PromptResponse.builder()
                .id(prompt.getId())
                .modelId(prompt.getModel().getId())
                .title(prompt.getTitle())
                .prompt(prompt.getPrompt())
                .negativePrompt(prompt.getNegativePrompt())
                .description(prompt.getDescription())
                .displayOrder(prompt.getDisplayOrder())
                .createdAt(prompt.getCreatedAt())
                .build();
    }
}
