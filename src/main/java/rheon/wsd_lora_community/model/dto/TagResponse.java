package rheon.wsd_lora_community.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import rheon.wsd_lora_community.model.entity.Tag;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "태그 정보 응답")
public class TagResponse {

    @Schema(description = "태그 ID", example = "1")
    private Long id;

    @Schema(description = "태그 이름", example = "애니메이션")
    private String name;

    @Schema(description = "사용 횟수", example = "150")
    private Integer usageCount;

    public static TagResponse from(Tag tag) {
        return TagResponse.builder()
                .id(tag.getId())
                .name(tag.getName())
                .usageCount(tag.getUsageCount())
                .build();
    }
}
