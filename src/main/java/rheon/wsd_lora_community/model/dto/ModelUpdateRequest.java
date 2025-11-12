package rheon.wsd_lora_community.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "LoRA 모델 수정 요청")
public class ModelUpdateRequest {

    @Size(max = 200, message = "제목은 200자 이하여야 합니다")
    @Schema(description = "모델 제목", example = "소녀 전선 캐릭터 LoRA v2")
    private String title;

    @Size(max = 5000, message = "설명은 5000자 이하여야 합니다")
    @Schema(description = "모델 설명", example = "업데이트된 설명입니다.")
    private String description;

    @Size(max = 100, message = "캐릭터 이름은 100자 이하여야 합니다")
    @Schema(description = "캐릭터 이름", example = "M4A1")
    private String characterName;

    @Size(max = 100, message = "스타일은 100자 이하여야 합니다")
    @Schema(description = "스타일", example = "애니메이션, 일러스트")
    private String style;

    @Schema(description = "공개 여부", example = "true")
    private Boolean isPublic;
}
