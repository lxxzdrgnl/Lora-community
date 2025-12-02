package rheon.wsd_lora_community.training.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * FastAPI로부터 학습 콜백 요청 DTO
 * - FastAPI가 숫자로 보내므로 Long 타입 사용
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "학습 콜백 요청")
public class TrainingCallbackRequest {

    @Schema(description = "사용자 ID", example = "0")
    private Long userId;

    @Schema(description = "모델 ID (선택)", example = "123")
    private Long modelId;

    @NotNull(message = "Job ID는 필수입니다")
    @Schema(description = "작업 ID (필수)", example = "16", required = true)
    private Long jobId;

    @Schema(description = "모델 이름 (SUCCESS 시)", example = "my-character-model")
    private String modelName;

    @Schema(description = "S3 폴더 경로 (학습 성공 시)", example = "user-1/my-character-model")
    private String s3Folder;

    @Schema(description = "S3 모델 파일 키 (학습 성공 시)", example = "user-1/my-character-model/my-character-model.safetensors")
    private String s3ModelKey;

    @Schema(description = "파일 크기 (bytes, 학습 성공 시)", example = "524288000")
    private Long fileSize;

    @NotBlank(message = "Status는 필수입니다")
    @Schema(description = "학습 상태 (필수)", required = true, example = "TRAINING",
            allowableValues = {"LOADING", "DOWNLOADING", "DOWNLOADING_COMPLETE", "PREPROCESSING",
                               "CAPTIONING_COMPLETE", "TRAINING", "UPLOADING", "SUCCESS", "FAIL"})
    private String status;

    @Schema(description = "메시지 (진행 중 또는 에러 메시지)", example = "Training 50/100")
    private String message;

    @Schema(description = "현재 에포크 (TRAINING 시)", example = "50")
    private Integer currentEpoch;

    @Schema(description = "에러 메시지 (학습 실패 시)", example = "Out of memory")
    private String error;
}
