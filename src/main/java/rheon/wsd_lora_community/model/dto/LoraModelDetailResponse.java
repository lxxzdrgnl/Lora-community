package rheon.wsd_lora_community.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import rheon.wsd_lora_community.model.entity.LoraModel;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "LoRA 모델 상세 응답")
public class LoraModelDetailResponse {

    @Schema(description = "모델 ID", example = "1")
    private Long id;

    @Schema(description = "유저 ID", example = "1")
    private Long userId;

    @Schema(description = "유저 닉네임", example = "길동이")
    private String userNickname;

    @Schema(description = "유저 프로필 이미지 URL", example = "https://...")
    private String userProfileImageUrl;

    @Schema(description = "제목", example = "소녀 전선 캐릭터 LoRA")
    private String title;

    @Schema(description = "설명", example = "소녀 전선 게임 캐릭터를 학습한 LoRA 모델입니다.")
    private String description;

    @Schema(description = "캐릭터 이름", example = "M4A1")
    private String characterName;

    @Schema(description = "스타일", example = "애니메이션, 일러스트")
    private String style;

    @Schema(description = "학습 이미지 수", example = "50")
    private Integer trainingImagesCount;

    @Schema(description = "에포크 수", example = "250")
    private Integer epochs;

    @Schema(description = "학습률", example = "0.0001")
    private BigDecimal learningRate;

    @Schema(description = "LoRA Rank", example = "8")
    private Integer loraRank;

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

    @Schema(description = "샘플 이미지 목록")
    private List<ModelSampleResponse> samples;

    @Schema(description = "프롬프트 예시 목록")
    private List<PromptResponse> prompts;

    @Schema(description = "태그 목록")
    private List<TagResponse> tags;

    @Schema(description = "현재 유저의 좋아요 여부", example = "true")
    private Boolean isLiked;

    @Schema(description = "생성일", example = "2025-01-13T10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "수정일", example = "2025-01-13T10:00:00")
    private LocalDateTime updatedAt;

    public static LoraModelDetailResponse from(LoraModel model,
                                                 List<ModelSampleResponse> samples,
                                                 List<PromptResponse> prompts,
                                                 List<TagResponse> tags,
                                                 Boolean isLiked) {
        return LoraModelDetailResponse.builder()
                .id(model.getId())
                .userId(model.getUser().getId())
                .userNickname(model.getUser().getNickname())
                .userProfileImageUrl(model.getUser().getProfileImageUrl())
                .title(model.getTitle())
                .description(model.getDescription())
                .characterName(model.getCharacterName())
                .style(model.getStyle())
                .trainingImagesCount(model.getTrainingImagesCount())
                .epochs(model.getEpochs())
                .learningRate(model.getLearningRate())
                .loraRank(model.getLoraRank())
                .baseModel(model.getBaseModel())
                .isPublic(model.getIsPublic())
                .status(model.getStatus().name())
                .viewCount(model.getViewCount())
                .likeCount(model.getLikeCount())
                .samples(samples)
                .prompts(prompts)
                .tags(tags)
                .isLiked(isLiked)
                .createdAt(model.getCreatedAt())
                .updatedAt(model.getUpdatedAt())
                .build();
    }
}
