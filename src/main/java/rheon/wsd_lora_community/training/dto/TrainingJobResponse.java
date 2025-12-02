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

    // 학습 파라미터 정보 (학습 시작 시 저장된 값)
    @Schema(description = "모델 이름", example = "My Custom LoRA")
    private String modelName;

    @Schema(description = "모델 설명", example = "A custom LoRA model for...")
    private String modelDescription;

    @Schema(description = "학습 이미지 개수", example = "20")
    private Integer trainingImagesCount;

    @Schema(description = "에포크 수", example = "250")
    private Integer epochs;

    @Schema(description = "학습률", example = "0.0001")
    private Double learningRate;

    @Schema(description = "LoRA 랭크", example = "32")
    private Integer loraRank;

    @Schema(description = "베이스 모델", example = "Lykon/AnyLoRA")
    private String baseModel;

    @Schema(description = "트리거 워드", example = "sksperson")
    private String triggerWord;

    @Schema(description = "모델 썸네일 URL", example = "https://...")
    private String modelThumbnailUrl;

    @Schema(description = "진행률 (0-100)", example = "75.5")
    private Double progressPercentage;

    public static TrainingJobResponse from(TrainingJob job) {
        // 진행률 계산
        Double progressPercentage = null;
        if (job.getTotalEpochs() != null && job.getTotalEpochs() > 0) {
            int currentEpoch = job.getCurrentEpoch() != null ? job.getCurrentEpoch() : 0;
            progressPercentage = (currentEpoch * 100.0) / job.getTotalEpochs();
        }

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
                // 학습 파라미터 정보
                .modelName(job.getModelName())
                .modelDescription(job.getModelDescription())
                .trainingImagesCount(job.getTrainingImagesCount())
                .epochs(job.getEpochs())
                .learningRate(job.getLearningRate())
                .loraRank(job.getLoraRank())
                .baseModel(job.getBaseModel())
                .triggerWord(job.getTriggerWord())
                .progressPercentage(progressPercentage)
                .build();
    }

    // 썸네일 URL 설정을 위한 정적 팩토리 메서드 (Service에서 사용)
    public static TrainingJobResponse from(TrainingJob job, String thumbnailUrl) {
        TrainingJobResponse response = from(job);
        return TrainingJobResponse.builder()
                .id(response.getId())
                .modelId(response.getModelId())
                .userId(response.getUserId())
                .status(response.getStatus())
                .currentEpoch(response.getCurrentEpoch())
                .totalEpochs(response.getTotalEpochs())
                .phase(response.getPhase())
                .errorMessage(response.getErrorMessage())
                .startedAt(response.getStartedAt())
                .completedAt(response.getCompletedAt())
                .createdAt(response.getCreatedAt())
                .modelName(response.getModelName())
                .modelDescription(response.getModelDescription())
                .trainingImagesCount(response.getTrainingImagesCount())
                .epochs(response.getEpochs())
                .learningRate(response.getLearningRate())
                .loraRank(response.getLoraRank())
                .baseModel(response.getBaseModel())
                .triggerWord(response.getTriggerWord())
                .progressPercentage(response.getProgressPercentage())
                .modelThumbnailUrl(thumbnailUrl)
                .build();
    }
}
