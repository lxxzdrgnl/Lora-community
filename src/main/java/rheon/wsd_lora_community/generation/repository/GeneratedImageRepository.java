package rheon.wsd_lora_community.generation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import rheon.wsd_lora_community.generation.entity.GeneratedImage;

import java.util.List;
import java.util.Optional;

/**
 * 생성된 이미지 Repository
 */
@Repository
public interface GeneratedImageRepository extends JpaRepository<GeneratedImage, Long> {

    /**
     * 특정 GenerationHistory에 속한 모든 이미지 조회 (표시 순서대로)
     */
    List<GeneratedImage> findByGenerationHistoryIdOrderByDisplayOrder(Long generationHistoryId);

    /**
     * 특정 GenerationHistory에 속한 이미지 개수 조회
     */
    long countByGenerationHistoryId(Long generationHistoryId);

    /**
     * 특정 GenerationHistory에 속한 모든 이미지 삭제
     */
    void deleteByGenerationHistoryId(Long generationHistoryId);

    /**
     * S3 키로 이미지 존재 여부 확인
     */
    boolean existsByS3Key(String s3Key);

    /**
     * 특정 모델의 특정 유저가 생성한 이미지들 조회 (최신순)
     */
    @Query("SELECT gi FROM GeneratedImage gi JOIN FETCH gi.generationHistory gh " +
            "WHERE gh.model.id = :modelId AND gh.user.id = :userId " +
            "ORDER BY gi.createdAt DESC")
    List<GeneratedImage> findByModelIdAndUserId(@Param("modelId") Long modelId, @Param("userId") Long userId);

    /**
     * 특정 이미지가 특정 모델에 속하는지 확인
     */
    @Query("SELECT CASE WHEN COUNT(gi) > 0 THEN true ELSE false END " +
            "FROM GeneratedImage gi JOIN gi.generationHistory gh " +
            "WHERE gi.id = :imageId AND gh.model.id = :modelId")
    boolean existsByIdAndModelId(@Param("imageId") Long imageId, @Param("modelId") Long modelId);

    /**
     * 이미지 ID와 모델 ID로 이미지 조회 (GenerationHistory fetch join)
     */
    @Query("SELECT gi FROM GeneratedImage gi JOIN FETCH gi.generationHistory gh " +
            "WHERE gi.id = :imageId AND gh.model.id = :modelId")
    Optional<GeneratedImage> findByIdAndModelId(@Param("imageId") Long imageId, @Param("modelId") Long modelId);
}
