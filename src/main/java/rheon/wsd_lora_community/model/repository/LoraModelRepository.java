package rheon.wsd_lora_community.model.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import rheon.wsd_lora_community.model.entity.LoraModel;
import rheon.wsd_lora_community.user.entity.User;

import java.util.Optional;

@Repository
public interface LoraModelRepository extends JpaRepository<LoraModel, Long> {

    // 공개 모델 페이징 조회
    Page<LoraModel> findByIsPublicTrueAndDeletedAtIsNull(Pageable pageable);

    // 유저별 모델 조회
    Page<LoraModel> findByUserAndDeletedAtIsNull(User user, Pageable pageable);

    // 모델 상세 조회 (삭제되지 않은 것만)
    Optional<LoraModel> findByIdAndDeletedAtIsNull(Long id);

    // 제목/설명 검색
    @Query("SELECT m FROM LoraModel m WHERE m.isPublic = true AND m.deletedAt IS NULL " +
            "AND (m.title LIKE %:keyword% OR m.description LIKE %:keyword%)")
    Page<LoraModel> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    // 태그로 검색
    @Query("SELECT DISTINCT m FROM LoraModel m " +
            "JOIN ModelTag mt ON mt.model = m " +
            "JOIN Tag t ON mt.tag = t " +
            "WHERE m.isPublic = true AND m.deletedAt IS NULL AND t.name IN :tagNames")
    Page<LoraModel> findByTags(@Param("tagNames") java.util.List<String> tagNames, Pageable pageable);

    // 인기 모델 (좋아요 순)
    Page<LoraModel> findByIsPublicTrueAndDeletedAtIsNullOrderByLikeCountDesc(Pageable pageable);

    // Service에서 사용하는 메서드들
    Page<LoraModel> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    @Query("SELECT DISTINCT m FROM LoraModel m JOIN m.modelTags mt WHERE mt.tag.id IN :tagIds AND m.isPublic = true AND m.deletedAt IS NULL")
    Page<LoraModel> findPublicModelsByTags(@Param("tagIds") java.util.List<Long> tagIds, Pageable pageable);

    @Query("SELECT m FROM LoraModel m WHERE m.isPublic = true AND m.deletedAt IS NULL " +
            "AND (LOWER(m.title) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(m.description) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<LoraModel> searchByTitleOrDescription(@Param("query") String query, Pageable pageable);

    // 유저의 모델 조회 (학습 완료된 모델만, 삭제되지 않은 것만)
    @Query("SELECT m FROM LoraModel m WHERE m.user.id = :userId AND m.deletedAt IS NULL ORDER BY m.createdAt DESC")
    Page<LoraModel> findByUserIdExcludingFailedOrderByCreatedAtDesc(@Param("userId") Long userId, Pageable pageable);

    // 가장 많이 조회된 모델 (관리자용)
    @Query("SELECT m FROM LoraModel m WHERE m.deletedAt IS NULL ORDER BY m.viewCount DESC")
    Page<LoraModel> findMostViewedModels(Pageable pageable);
}
