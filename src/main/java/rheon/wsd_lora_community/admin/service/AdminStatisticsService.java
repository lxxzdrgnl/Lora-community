package rheon.wsd_lora_community.admin.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rheon.wsd_lora_community.admin.dto.ModelStatisticsResponse;
import rheon.wsd_lora_community.admin.dto.ModelUsageStatistics;
import rheon.wsd_lora_community.generation.repository.GenerationHistoryRepository;
import rheon.wsd_lora_community.model.entity.LoraModel;
import rheon.wsd_lora_community.model.repository.LoraModelRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 관리자용 통계 서비스
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AdminStatisticsService {

    private final LoraModelRepository loraModelRepository;
    private final GenerationHistoryRepository generationHistoryRepository;

    /**
     * 최근 일주일간 가장 많이 사용된 모델 (페이징)
     */
    public Page<ModelUsageStatistics> getMostUsedModelsInLastWeek(Pageable pageable) {
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
        List<ModelUsageStatistics> statistics = generationHistoryRepository.findMostUsedModelsInLastWeek(weekAgo, pageable);

        // List를 Page로 변환 (totalElements는 실제 DB 조회가 필요하므로 현재 리스트 크기 사용)
        return new PageImpl<>(statistics, pageable, statistics.size());
    }

    /**
     * 가장 많이 조회된 모델 (페이징)
     */
    public Page<ModelStatisticsResponse> getMostViewedModels(Pageable pageable) {
        Page<LoraModel> models = loraModelRepository.findMostViewedModels(pageable);
        return models.map(ModelStatisticsResponse::from);
    }
}
