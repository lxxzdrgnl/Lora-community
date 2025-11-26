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
@Schema(description = "LoRA 모델 생성 요청")
public class ModelCreateRequest {

    @NotBlank(message = "제목은 필수입니다")
    @Size(max = 200, message = "제목은 200자 이하여야 합니다")
    @Schema(description = "모델 제목", example = "소녀 전선 캐릭터 LoRA")
    private String title;

    @Size(max = 5000, message = "설명은 5000자 이하여야 합니다")
    @Schema(description = "모델 설명", example = "소녀 전선 게임 캐릭터를 학습한 LoRA 모델입니다.")
    private String description;

    @Schema(description = "학습 이미지 수", example = "50")
    private Integer trainingImagesCount;

    @Schema(description = "학습 에포크 수", example = "250")
    private Integer epochs;

    @Schema(description = "학습률", example = "0.0001")
    private Double learningRate;

    @Schema(description = "LoRA Rank", example = "8")
    private Integer loraRank;

    @Size(max = 200, message = "베이스 모델은 200자 이하여야 합니다")
    @Schema(description = "베이스 모델", example = "stablediffusionapi/anything-v5")
    private String baseModel;

    @Schema(description = "공개 여부", example = "false")
    private Boolean isPublic;
}
