package rheon.wsd_lora_community.community.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rheon.wsd_lora_community.community.dto.CommentCreateRequest;
import rheon.wsd_lora_community.community.dto.CommentResponse;
import rheon.wsd_lora_community.community.entity.Comment;
import rheon.wsd_lora_community.community.entity.CommentLike;
import rheon.wsd_lora_community.community.repository.CommentLikeRepository;
import rheon.wsd_lora_community.community.repository.CommentRepository;
import rheon.wsd_lora_community.global.dto.PageResponse;
import rheon.wsd_lora_community.global.exception.CustomException;
import rheon.wsd_lora_community.global.exception.ErrorCode;
import rheon.wsd_lora_community.model.entity.LoraModel;
import rheon.wsd_lora_community.model.repository.LoraModelRepository;
import rheon.wsd_lora_community.user.entity.User;
import rheon.wsd_lora_community.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 댓글 관리 서비스
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final LoraModelRepository loraModelRepository;
    private final UserRepository userRepository;

    /**
     * 모델의 댓글 목록 조회 (페이징, 부모 댓글만)
     */
    public PageResponse<CommentResponse> getCommentsByModel(Long modelId, Long currentUserId, Pageable pageable) {
        LoraModel model = loraModelRepository.findById(modelId)
                .orElseThrow(() -> new CustomException(ErrorCode.MODEL_NOT_FOUND));

        Page<Comment> commentsPage = commentRepository.findByModelAndParentCommentIsNullAndDeletedAtIsNull(model, pageable);

        List<CommentResponse> responses = commentsPage.getContent().stream()
                .map(comment -> {
                    // 좋아요 여부 확인
                    boolean isLiked = currentUserId != null &&
                            userRepository.findById(currentUserId)
                                    .map(user -> commentLikeRepository.existsByCommentAndUser(comment, user))
                                    .orElse(false);

                    // 대댓글 조회
                    List<CommentResponse> replies = getRepliesForComment(comment, currentUserId);

                    return CommentResponse.from(comment, isLiked, replies);
                })
                .collect(Collectors.toList());

        return PageResponse.of(commentsPage, responses);
    }

    /**
     * 대댓글 목록 조회
     */
    private List<CommentResponse> getRepliesForComment(Comment parentComment, Long currentUserId) {
        List<Comment> replies = commentRepository.findByParentCommentAndDeletedAtIsNull(parentComment);

        return replies.stream()
                .map(reply -> {
                    boolean isLiked = currentUserId != null &&
                            userRepository.findById(currentUserId)
                                    .map(user -> commentLikeRepository.existsByCommentAndUser(reply, user))
                                    .orElse(false);

                    return CommentResponse.from(reply, isLiked, null); // 대대댓글은 지원하지 않음
                })
                .collect(Collectors.toList());
    }

    /**
     * 댓글 단건 조회
     */
    public CommentResponse getComment(Long commentId, Long currentUserId) {
        Comment comment = commentRepository.findByIdAndDeletedAtIsNull(commentId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));

        // 좋아요 여부 확인
        boolean isLiked = currentUserId != null &&
                userRepository.findById(currentUserId)
                        .map(user -> commentLikeRepository.existsByCommentAndUser(comment, user))
                        .orElse(false);

        // 대댓글 조회 (부모 댓글인 경우만)
        List<CommentResponse> replies = null;
        if (!comment.isReply()) {
            replies = getRepliesForComment(comment, currentUserId);
        }

        return CommentResponse.from(comment, isLiked, replies);
    }

    /**
     * 댓글 작성
     */
    @Transactional
    public CommentResponse createComment(Long modelId, Long userId, CommentCreateRequest request) {
        LoraModel model = loraModelRepository.findById(modelId)
                .orElseThrow(() -> new CustomException(ErrorCode.MODEL_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 부모 댓글 확인 (대댓글인 경우)
        Comment parentComment = null;
        if (request.getParentCommentId() != null) {
            parentComment = commentRepository.findByIdAndDeletedAtIsNull(request.getParentCommentId())
                    .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));

            // 대댓글의 대댓글은 허용하지 않음
            if (parentComment.isReply()) {
                throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
            }
        }

        Comment comment = Comment.builder()
                .model(model)
                .user(user)
                .parentComment(parentComment)
                .content(request.getContent())
                .likeCount(0)
                .build();

        Comment saved = commentRepository.save(comment);

        return CommentResponse.from(saved, false, null);
    }

    /**
     * 댓글 수정
     */
    @Transactional
    public CommentResponse updateComment(Long commentId, Long userId, String content) {
        Comment comment = commentRepository.findByIdAndDeletedAtIsNull(commentId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 권한 확인: 작성자만 수정 가능
        if (!comment.isOwner(user)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        comment.updateContent(content);

        // 좋아요 여부 확인
        boolean isLiked = commentLikeRepository.existsByCommentAndUser(comment, user);

        // 대댓글 조회 (부모 댓글인 경우만)
        List<CommentResponse> replies = null;
        if (!comment.isReply()) {
            replies = getRepliesForComment(comment, userId);
        }

        return CommentResponse.from(comment, isLiked, replies);
    }

    /**
     * 댓글 삭제 (Soft Delete)
     */
    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findByIdAndDeletedAtIsNull(commentId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 권한 확인: 작성자만 삭제 가능
        if (!comment.isOwner(user)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        comment.softDelete();
    }

    /**
     * 댓글 좋아요 토글
     */
    @Transactional
    public boolean toggleCommentLike(Long commentId, Long userId) {
        Comment comment = commentRepository.findByIdAndDeletedAtIsNull(commentId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 이미 좋아요한 경우 취소
        if (commentLikeRepository.existsByCommentAndUser(comment, user)) {
            commentLikeRepository.deleteByCommentAndUser(comment, user);
            comment.decrementLikeCount();
            return false; // 좋아요 취소
        }

        // 좋아요 추가
        commentLikeRepository.save(
                CommentLike.builder()
                        .comment(comment)
                        .user(user)
                        .build()
        );
        comment.incrementLikeCount();
        return true; // 좋아요 추가
    }
}
