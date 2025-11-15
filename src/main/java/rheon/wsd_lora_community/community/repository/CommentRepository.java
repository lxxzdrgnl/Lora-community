package rheon.wsd_lora_community.community.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rheon.wsd_lora_community.community.entity.Comment;
import rheon.wsd_lora_community.model.entity.LoraModel;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    Page<Comment> findByModelIdAndDeletedAtIsNullOrderByCreatedAtDesc(Long modelId, Pageable pageable);

    Optional<Comment> findByIdAndDeletedAtIsNull(Long id);
}
