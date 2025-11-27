package rheon.wsd_lora_community.generation.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import rheon.wsd_lora_community.generation.dto.AvailableModelResponse;
import rheon.wsd_lora_community.generation.entity.GenerationHistory;
import rheon.wsd_lora_community.model.entity.LoraModel;
import rheon.wsd_lora_community.user.entity.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface GenerationHistoryRepository extends JpaRepository<GenerationHistory, Long> {

    Page<GenerationHistory> findByUser(User user, Pageable pageable);

    Page<GenerationHistory> findByModel(LoraModel model, Pageable pageable);

    Page<GenerationHistory> findByUserAndModel(User user, LoraModel model, Pageable pageable);

    /**
     * 사용자의 진행 중인 생성 작업 조회 (GENERATING 상태)
     */
    Optional<GenerationHistory> findFirstByUserAndStatusOrderByCreatedAtDesc(User user, String status);

    /**
     * 사용자가 이미지 생성에 사용한 모델 목록 조회 (중복 제거, 최근 사용 순)
     * - 생성 히스토리 필터링 드롭다운용
     */
    @Query("SELECT new rheon.wsd_lora_community.generation.dto.AvailableModelResponse(m.id, m.title) " +
            "FROM GenerationHistory gh JOIN gh.model m " +
            "WHERE gh.user = :user " +
            "GROUP BY m.id, m.title " +
            "ORDER BY MAX(gh.createdAt) DESC")
    List<AvailableModelResponse> findAvailableModelsByUser(@Param("user") User user);
}
