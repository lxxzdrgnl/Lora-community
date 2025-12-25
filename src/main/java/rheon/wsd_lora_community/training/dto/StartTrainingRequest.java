package rheon.wsd_lora_community.training.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 학습 시작 요청 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "학습 시작 요청")
public class StartTrainingRequest {

    @Schema(description = "모델 이름", example = "My Custom LoRA", required = true)
    @NotBlank(message = "모델 이름은 필수입니다.")
    private String modelName;

    @Schema(description = "모델 설명", example = "A custom LoRA model for anime characters")
    private String modelDescription;

    @Schema(description = "학습 이미지 URL 리스트 (S3 Presigned URL)", example = "[\"https://s3.amazonaws.com/bucket/image1.png\", \"https://s3.amazonaws.com/bucket/image2.png\"]", required = true)
    @NotEmpty(message = "학습 이미지 URL 리스트는 필수입니다.")
    private List<String> trainingImageUrls;

    @Schema(description = "트리거 워드 (프롬프트에서 모델을 활성화할 키워드)", example = "sks")
    private String triggerWord;

    @Schema(description = "학습 에포크 수", example = "10", required = true, minimum = "1")
    @Min(value = 1, message = "에포크는 최소 1 이상이어야 합니다.")
    private Integer epochs = 10;

    @Schema(description = "학습률 (Learning Rate)", example = "0.0001")
    private Double learningRate;

    @Schema(description = "LoRA Rank (16, 32, 64)", example = "32")
    private Integer loraRank;

    @Schema(description = "베이스 모델 (HuggingFace 모델 ID)", example = "stablediffusionapi/anything-v5")
    private String baseModel = "stablediffusionapi/anything-v5";

    @Schema(description = "전처리 스킵 여부", example = "false")
    private Boolean skipPreprocessing = false;
}
