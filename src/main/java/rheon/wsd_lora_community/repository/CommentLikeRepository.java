package rheon.wsd_lora_community.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rheon.wsd_lora_community.entity.community.Comment;
import rheon.wsd_lora_community.entity.community.CommentLike;
import rheon.wsd_lora_community.entity.user.User;

import java.util.Optional;

@Repository
public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {

    Optional<CommentLike> findByCommentAndUser(Comment comment, User user);

    boolean existsByCommentAndUser(Comment comment, User user);

    void deleteByCommentAndUser(Comment comment, User user);
}
