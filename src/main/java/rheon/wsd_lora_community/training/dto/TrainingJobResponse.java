package rheon.wsd_lora_community.training.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import rheon.wsd_lora_community.training.entity.TrainingJob;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "학습 작업 응답")
public class TrainingJobResponse {

    @Schema(description = "학습 작업 ID", example = "1")
    private Long id;

    @Schema(description = "모델 ID", example = "1")
    private Long modelId;

    @Schema(description = "유저 ID", example = "1")
    private Long userId;

    @Schema(description = "상태", example = "TRAINING")
    private String status;

    @Schema(description = "현재 에포크", example = "50")
    private Integer currentEpoch;

    @Schema(description = "전체 에포크 수", example = "250")
    private Integer totalEpochs;

    @Schema(description = "현재 단계", example = "Training phase")
    private String phase;

    @Schema(description = "에러 메시지", example = "null")
    private String errorMessage;

    @Schema(description = "시작 시간", example = "2025-01-13T10:00:00")
    private LocalDateTime startedAt;

    @Schema(description = "완료 시간", example = "2025-01-13T12:00:00")
    private LocalDateTime completedAt;

    @Schema(description = "생성일", example = "2025-01-13T10:00:00")
    private LocalDateTime createdAt;

    public static TrainingJobResponse from(TrainingJob job) {
        return TrainingJobResponse.builder()
                .id(job.getId())
                .modelId(job.getModel() != null ? job.getModel().getId() : null)
                .userId(job.getUser().getId())
                .status(job.getStatus().name())
                .currentEpoch(job.getCurrentEpoch())
                .totalEpochs(job.getTotalEpochs())
                .phase(job.getPhase())
                .errorMessage(job.getErrorMessage())
                .startedAt(job.getStartedAt())
                .completedAt(job.getCompletedAt())
                .createdAt(job.getCreatedAt())
                .build();
    }
}
