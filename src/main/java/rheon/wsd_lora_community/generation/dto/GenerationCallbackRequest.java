package rheon.wsd_lora_community.generation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 이미지 생성 콜백 요청 DTO (FastAPI → Spring Boot)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "이미지 생성 콜백 요청 (FastAPI 전용)")
public class GenerationCallbackRequest {

    @Schema(description = "상태 (GENERATING, SUCCESS, FAIL)", example = "SUCCESS", required = true)
    private String status;

    @Schema(description = "생성 기록 ID", example = "123")
    private Long historyId;

    @Schema(description = "사용자 ID", example = "456")
    private Long userId;

    @Schema(description = "모델 ID", example = "789")
    private Long modelId;

    @Schema(description = "프롬프트", example = "a beautiful landscape")
    private String prompt;

    @Schema(description = "네거티브 프롬프트", example = "ugly, blurry")
    private String negativePrompt;

    @Schema(description = "생성 스텝 수", example = "50")
    private Integer steps;

    @Schema(description = "Guidance Scale", example = "7.5")
    private Double guidanceScale;

    @Schema(description = "LoRA Scale", example = "1.0")
    private Double loraScale;

    @Schema(description = "시드 값", example = "12345")
    private Long seed;

    @Schema(description = "생성할 이미지 수", example = "4")
    private Integer numImages;

    @Schema(description = "현재 진행 스텝", example = "25")
    private Integer currentStep;

    @Schema(description = "전체 스텝 수", example = "50")
    private Integer totalSteps;

    @Schema(description = "진행 메시지", example = "Generating image 1/4")
    private String message;

    @Schema(description = "생성된 이미지 S3 키 목록", example = "[\"images/user123/gen_1.png\", \"images/user123/gen_2.png\"]")
    private List<String> imageS3Keys;

    @Schema(description = "에러 메시지", example = "GPU memory error")
    private String error;
}
