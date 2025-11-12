package rheon.wsd_lora_community.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rheon.wsd_lora_community.entity.community.Comment;
import rheon.wsd_lora_community.entity.model.LoraModel;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    Page<Comment> findByModelAndParentCommentIsNullAndDeletedAtIsNull(LoraModel model, Pageable pageable);

    List<Comment> findByParentCommentAndDeletedAtIsNull(Comment parentComment);

    Optional<Comment> findByIdAndDeletedAtIsNull(Long id);
}
