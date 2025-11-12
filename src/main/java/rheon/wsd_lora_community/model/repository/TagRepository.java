package rheon.wsd_lora_community.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import rheon.wsd_lora_community.model.entity.Tag;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

    Optional<Tag> findByName(String name);

    boolean existsByName(String name);

    List<Tag> findByCategory(Tag.TagCategory category);

    @Query("SELECT t FROM Tag t WHERE t.name LIKE %:search%")
    List<Tag> searchByName(@Param("search") String search);

    // 인기 태그 (사용 횟수 순)
    List<Tag> findTop20ByOrderByUsageCountDesc();
}
