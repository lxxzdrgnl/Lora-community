package rheon.wsd_lora_community.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 모델 사용 통계 DTO (생성 횟수 기준)
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelUsageStatistics {
    private Long modelId;
    private String title;
    private Long usageCount;  // 최근 일주일간 사용 횟수
    private String ownerNickname;
}
