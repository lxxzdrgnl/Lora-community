package rheon.wsd_lora_community.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import rheon.wsd_lora_community.model.entity.ModelSample;

import java.util.List;
import java.util.Optional;

/**
 * 모델 샘플 Repository
 */
@Repository
public interface ModelSampleRepository extends JpaRepository<ModelSample, Long> {

    /**
     * 모델의 샘플 이미지 목록 조회 (표시 순서대로)
     * - GeneratedImage와 GenerationHistory를 함께 fetch (N+1 방지)
     */
    @Query("SELECT ms FROM ModelSample ms " +
            "JOIN FETCH ms.generatedImage gi " +
            "JOIN FETCH gi.generationHistory gh " +
            "WHERE ms.model.id = :modelId " +
            "ORDER BY ms.displayOrder ASC")
    List<ModelSample> findByModelIdOrderByDisplayOrder(@Param("modelId") Long modelId);

    /**
     * 모델의 대표 샘플 이미지 조회
     */
    @Query("SELECT ms FROM ModelSample ms " +
            "JOIN FETCH ms.generatedImage gi " +
            "WHERE ms.model.id = :modelId AND ms.isPrimary = true")
    Optional<ModelSample> findPrimarySampleByModelId(@Param("modelId") Long modelId);

    /**
     * 특정 생성 이미지가 이미 샘플로 등록되어 있는지 확인
     */
    boolean existsByModelIdAndGeneratedImageId(Long modelId, Long generatedImageId);

    /**
     * 생성 이미지가 샘플로 등록되어 있는지 확인 (모델 무관)
     */
    boolean existsByGeneratedImageId(Long generatedImageId);

    /**
     * 모델의 샘플 개수 조회
     */
    long countByModelId(Long modelId);

    /**
     * 특정 샘플이 특정 모델에 속하는지 확인
     */
    boolean existsByIdAndModelId(Long sampleId, Long modelId);
}
