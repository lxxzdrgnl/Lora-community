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

    @Schema(description = "공개 여부", example = "true")
    private Boolean isPublic;

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
                .isPublic(model.getIsPublic())
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
