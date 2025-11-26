package rheon.wsd_lora_community.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import rheon.wsd_lora_community.model.entity.LoraModel;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "LoRA 모델 응답 (목록용)")
public class LoraModelResponse {

    @Schema(description = "모델 ID", example = "1")
    private Long id;

    @Schema(description = "유저 ID", example = "1")
    private Long userId;

    @Schema(description = "유저 닉네임", example = "길동이")
    private String userNickname;

    @Schema(description = "제목", example = "소녀 전선 캐릭터 LoRA")
    private String title;

    @Schema(description = "설명", example = "소녀 전선 게임 캐릭터를 학습한 LoRA 모델입니다.")
    private String description;

    @Schema(description = "학습 이미지 수", example = "50")
    private Integer trainingImagesCount;

    @Schema(description = "베이스 모델", example = "stablediffusionapi/anything-v5")
    private String baseModel;

    @Schema(description = "공개 여부", example = "true")
    private Boolean isPublic;

    @Schema(description = "상태", example = "COMPLETED")
    private String status;

    @Schema(description = "조회수", example = "150")
    private Integer viewCount;

    @Schema(description = "좋아요 수", example = "30")
    private Integer likeCount;

    @Schema(description = "즐겨찾기 수", example = "20")
    private Integer favoriteCount;

    @Schema(description = "현재 유저의 좋아요 여부", example = "false")
    private Boolean isLiked;

    @Schema(description = "썸네일 이미지 URL (대표 샘플 이미지)", example = "https://...")
    private String thumbnailUrl;

    @Schema(description = "S3 키 (모델 저장 경로)", example = "models/1/my-character-model.safetensors")
    private String s3Key;

    @Schema(description = "모델 파일 크기 (bytes)", example = "524288000")
    private Long fileSize;

    @Schema(description = "모델 다운로드용 Presigned URL (유효시간: 1시간)", example = "https://...")
    private String modelDownloadUrl;

    @Schema(description = "생성일", example = "2025-01-13T10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "수정일", example = "2025-01-13T10:00:00")
    private LocalDateTime updatedAt;

    public static LoraModelResponse from(LoraModel model) {
        return from(model, null, null);
    }

    public static LoraModelResponse from(LoraModel model, Boolean isLiked, String thumbnailUrl) {
        return LoraModelResponse.builder()
                .id(model.getId())
                .userId(model.getUser().getId())
                .userNickname(model.getUser().getNickname())
                .title(model.getTitle())
                .description(model.getDescription())
                .trainingImagesCount(model.getTrainingImagesCount())
                .baseModel(model.getBaseModel())
                .isPublic(model.getIsPublic())
                .status(model.getStatus().name())
                .viewCount(model.getViewCount())
                .likeCount(model.getLikeCount())
                .isLiked(isLiked)
                .thumbnailUrl(thumbnailUrl)
                .s3Key(model.getS3Key())
                .fileSize(model.getFileSize())
                .createdAt(model.getCreatedAt())
                .updatedAt(model.getUpdatedAt())
                .build();
    }

    /**
     * Presigned URL을 포함한 응답 생성
     */
    public static LoraModelResponse fromWithPresignedUrl(LoraModel model, String presignedUrl) {
        LoraModelResponse response = from(model);
        response.modelDownloadUrl = presignedUrl;
        return response;
    }

    /**
     * Presigned URL 직접 설정 (Builder 패턴 대신)
     */
    public void setModelDownloadUrl(String modelDownloadUrl) {
        this.modelDownloadUrl = modelDownloadUrl;
    }
}
