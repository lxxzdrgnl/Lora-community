package rheon.wsd_lora_community.generation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rheon.wsd_lora_community.generation.entity.GeneratedImage;

import java.util.List;

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
}
