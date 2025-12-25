package rheon.wsd_lora_community.admin.dto;

import lombok.Builder;
import lombok.Getter;
import rheon.wsd_lora_community.model.entity.LoraModel;

import java.time.LocalDateTime;

/**
 * 모델 통계 응답 DTO (조회수 기준)
 */
@Getter
@Builder
public class ModelStatisticsResponse {
    private Long modelId;
    private String title;
    private String characterName;
    private Integer viewCount;
    private Integer likeCount;
    private String ownerNickname;
    private LocalDateTime createdAt;

    public static ModelStatisticsResponse from(LoraModel model) {
        return ModelStatisticsResponse.builder()
                .modelId(model.getId())
                .title(model.getTitle())
                .viewCount(model.getViewCount())
                .likeCount(model.getLikeCount())
                .ownerNickname(model.getUser().getNickname())
                .createdAt(model.getCreatedAt())
                .build();
    }
}
