package rheon.wsd_lora_community.training.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * FastAPI로부터 학습 완료 콜백 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "학습 완료 콜백 요청")
public class TrainingCallbackRequest {

    @NotBlank(message = "User ID는 필수입니다")
    @Schema(description = "사용자 ID", example = "1")
    private String userId;

    @NotBlank(message = "Model name은 필수입니다")
    @Schema(description = "모델 이름", example = "my-character-model")
    private String modelName;

    @Schema(description = "S3 폴더 경로 (학습 성공 시)", example = "user-1/my-character-model")
    private String s3Folder;

    @Schema(description = "S3 모델 파일 키 (학습 성공 시)", example = "user-1/my-character-model/my-character-model.safetensors")
    private String s3ModelKey;

    @Schema(description = "파일 크기 (bytes, 학습 성공 시)", example = "524288000")
    private Long fileSize;

    @NotBlank(message = "Status는 필수입니다")
    @Schema(description = "학습 상태", example = "SUCCESS", allowableValues = {"SUCCESS", "FAIL"})
    private String status;

    @Schema(description = "에러 메시지 (학습 실패 시)", example = "Out of memory")
    private String error;
}
