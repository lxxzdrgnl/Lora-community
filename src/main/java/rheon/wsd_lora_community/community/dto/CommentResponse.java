package rheon.wsd_lora_community.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import rheon.wsd_lora_community.community.entity.Comment;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "댓글 응답")
public class CommentResponse {

    @Schema(description = "댓글 ID", example = "1")
    private Long id;

    @Schema(description = "모델 ID", example = "1")
    private Long modelId;

    @Schema(description = "작성자 ID", example = "1")
    private Long userId;

    @Schema(description = "작성자 닉네임", example = "길동이")
    private String userNickname;

    @Schema(description = "작성자 프로필 이미지 URL", example = "https://example.com/profile.jpg")
    private String userProfileImageUrl;

    @Schema(description = "부모 댓글 ID (대댓글인 경우)", example = "null")
    private Long parentCommentId;

    @Schema(description = "댓글 내용", example = "정말 훌륭한 모델이네요!")
    private String content;

    @Schema(description = "좋아요 수", example = "5")
    private Integer likeCount;

    @Schema(description = "현재 유저의 좋아요 여부", example = "false")
    private Boolean isLiked;

    @Schema(description = "대댓글 목록")
    private List<CommentResponse> replies;

    @Schema(description = "삭제 여부", example = "false")
    private Boolean isDeleted;

    @Schema(description = "생성일", example = "2025-01-13T10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "수정일", example = "2025-01-13T10:00:00")
    private LocalDateTime updatedAt;

    public static CommentResponse from(Comment comment, Boolean isLiked, List<CommentResponse> replies) {
        return CommentResponse.builder()
                .id(comment.getId())
                .modelId(comment.getModel().getId())
                .userId(comment.getUser().getId())
                .userNickname(comment.getUser().getNickname())
                .userProfileImageUrl(comment.getUser().getProfileImageUrl())
                .parentCommentId(comment.getParentComment() != null ? comment.getParentComment().getId() : null)
                .content(comment.getContent())
                .likeCount(comment.getLikeCount())
                .isLiked(isLiked)
                .replies(replies)
                .isDeleted(comment.getDeletedAt() != null)
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }
}
