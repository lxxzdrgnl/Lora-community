package rheon.wsd_lora_community.community.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import rheon.wsd_lora_community.community.dto.CommentCreateRequest;
import rheon.wsd_lora_community.community.dto.CommentResponse;
import rheon.wsd_lora_community.community.service.CommentService;
import rheon.wsd_lora_community.community.service.LikeService;
import rheon.wsd_lora_community.global.dto.ApiResponse;
import rheon.wsd_lora_community.global.dto.ErrorResponse;
import rheon.wsd_lora_community.global.dto.PageResponse;
import rheon.wsd_lora_community.model.dto.LoraModelResponse;

import java.util.Map;

/**
 * 커뮤니티 기능 컨트롤러
 * - 댓글/대댓글 CRUD
 * - 댓글 좋아요
 * - 모델 좋아요/즐겨찾기
 */
@RestController
@RequestMapping("/api/models")
@RequiredArgsConstructor
@Tag(name = "Community", description = "커뮤니티 API (댓글, 좋아요, 즐겨찾기)")
public class CommentController {

    private final CommentService commentService;
    private final LikeService likeService;

    /**
     * Authentication 객체에서 사용자 ID 추출
     * OAuth2User 또는 UserDetails 모두 지원
     */
    private Long getUserIdFromAuthentication(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return null; // 비로그인 사용자
        }

        Object principal = authentication.getPrincipal();

        // OAuth2 로그인 (Google)
        if (principal instanceof OAuth2User) {
            OAuth2User oauth2User = (OAuth2User) principal;
            return Long.valueOf(oauth2User.getAttribute("id").toString());
        }

        // JWT 인증
        if (principal instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) principal;
            return Long.valueOf(userDetails.getUsername());
        }

        return null;
    }

    // ========== 댓글 관리 ==========

    /**
     * 모델의 댓글 목록 조회
     */
    @GetMapping("/{modelId}/comments")
    @Operation(summary = "댓글 목록 조회", description = "모델의 댓글 목록을 조회합니다. (부모 댓글 + 대댓글)")
    public ResponseEntity<ApiResponse<PageResponse<CommentResponse>>> getComments(
            @Parameter(description = "모델 ID", required = true)
            @PathVariable Long modelId,
            @Parameter(hidden = true)
            Authentication authentication,
            @ParameterObject
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Long currentUserId = getUserIdFromAuthentication(authentication);

        PageResponse<CommentResponse> comments = commentService.getCommentsByModel(modelId, currentUserId, pageable);

        return ResponseEntity.ok(
                ApiResponse.success("댓글 목록 조회 성공", comments)
        );
    }

    /**
     * 댓글 단건 조회
     */
    @GetMapping("/{modelId}/comments/{commentId}")
    @Operation(summary = "댓글 상세 조회", description = "특정 댓글의 상세 정보를 조회합니다.")
    public ResponseEntity<ApiResponse<CommentResponse>> getComment(
            @Parameter(description = "모델 ID", required = true)
            @PathVariable Long modelId,
            @Parameter(description = "댓글 ID", required = true)
            @PathVariable Long commentId,
            @Parameter(hidden = true)
            Authentication authentication
    ) {
        Long currentUserId = getUserIdFromAuthentication(authentication);

        CommentResponse comment = commentService.getComment(commentId, currentUserId);

        return ResponseEntity.ok(
                ApiResponse.success("댓글 조회 성공", comment)
        );
    }

    /**
     * 댓글 작성
     */
    @PostMapping("/{modelId}/comments")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "댓글 작성", description = "모델에 댓글을 작성합니다. parentCommentId가 있으면 대댓글이 됩니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "댓글 작성 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 (유효성 검증 실패)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "모델을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<CommentResponse>> createComment(
            @Parameter(description = "모델 ID", required = true)
            @PathVariable Long modelId,
            @Parameter(hidden = true)
            Authentication authentication,
            @Valid @RequestBody CommentCreateRequest request
    ) {
        Long userId = getUserIdFromAuthentication(authentication);
        if (userId == null) {
            throw new IllegalArgumentException("인증 정보가 없습니다.");
        }

        CommentResponse comment = commentService.createComment(modelId, userId, request);

        return ResponseEntity.ok(
                ApiResponse.success("댓글 작성 성공", comment)
        );
    }

    /**
     * 댓글 수정
     */
    @PutMapping("/{modelId}/comments/{commentId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "댓글 수정", description = "댓글을 수정합니다. (작성자만 가능)")
    public ResponseEntity<ApiResponse<CommentResponse>> updateComment(
            @Parameter(description = "모델 ID", required = true)
            @PathVariable Long modelId,
            @Parameter(description = "댓글 ID", required = true)
            @PathVariable Long commentId,
            @Parameter(hidden = true)
            Authentication authentication,
            @RequestBody Map<String, String> request
    ) {
        Long userId = getUserIdFromAuthentication(authentication);
        if (userId == null) {
            throw new IllegalArgumentException("인증 정보가 없습니다.");
        }

        String content = request.get("content");

        CommentResponse updatedComment = commentService.updateComment(commentId, userId, content);

        return ResponseEntity.ok(
                ApiResponse.success("댓글 수정 성공", updatedComment)
        );
    }

    /**
     * 댓글 삭제
     */
    @DeleteMapping("/{modelId}/comments/{commentId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "댓글 삭제", description = "댓글을 삭제합니다. (작성자만 가능)")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @Parameter(description = "모델 ID", required = true)
            @PathVariable Long modelId,
            @Parameter(description = "댓글 ID", required = true)
            @PathVariable Long commentId,
            @Parameter(hidden = true)
            Authentication authentication
    ) {
        Long userId = getUserIdFromAuthentication(authentication);
        if (userId == null) {
            throw new IllegalArgumentException("인증 정보가 없습니다.");
        }

        commentService.deleteComment(commentId, userId);

        return ResponseEntity.ok(
                ApiResponse.success("댓글 삭제 성공")
        );
    }

    /**
     * 댓글 좋아요 토글
     */
    @PostMapping("/{modelId}/comments/{commentId}/like")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "댓글 좋아요 토글", description = "댓글에 좋아요를 누르거나 취소합니다.")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> toggleCommentLike(
            @Parameter(description = "모델 ID", required = true)
            @PathVariable Long modelId,
            @Parameter(description = "댓글 ID", required = true)
            @PathVariable Long commentId,
            @Parameter(hidden = true)
            Authentication authentication
    ) {
        Long userId = getUserIdFromAuthentication(authentication);
        if (userId == null) {
            throw new IllegalArgumentException("인증 정보가 없습니다.");
        }

        boolean isLiked = commentService.toggleCommentLike(commentId, userId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        isLiked ? "좋아요 추가 성공" : "좋아요 취소 성공",
                        Map.of("isLiked", isLiked)
                )
        );
    }

    // ========== 모델 좋아요 ==========

    /**
     * 모델 좋아요 토글
     */
    @PostMapping("/{modelId}/like")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "모델 좋아요 토글", description = "모델에 좋아요를 누르거나 취소합니다.")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> toggleModelLike(
            @Parameter(description = "모델 ID", required = true)
            @PathVariable Long modelId,
            @Parameter(hidden = true)
            Authentication authentication
    ) {
        Long userId = getUserIdFromAuthentication(authentication);
        if (userId == null) {
            throw new IllegalArgumentException("인증 정보가 없습니다.");
        }

        boolean isLiked = likeService.toggleModelLike(modelId, userId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        isLiked ? "좋아요 추가 성공" : "좋아요 취소 성공",
                        Map.of("isLiked", isLiked)
                )
        );
    }

    /**
     * 모델 좋아요 여부 확인
     */
    @GetMapping("/{modelId}/like/status")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "모델 좋아요 여부 확인", description = "현재 유저가 모델에 좋아요를 눌렀는지 확인합니다.")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> checkModelLike(
            @Parameter(description = "모델 ID", required = true)
            @PathVariable Long modelId,
            @Parameter(hidden = true)
            Authentication authentication
    ) {
        Long userId = getUserIdFromAuthentication(authentication);
        if (userId == null) {
            throw new IllegalArgumentException("인증 정보가 없습니다.");
        }

        boolean isLiked = likeService.isModelLikedByUser(modelId, userId);

        return ResponseEntity.ok(
                ApiResponse.success("좋아요 여부 조회 성공", Map.of("isLiked", isLiked))
        );
    }

    // ========== 모델 즐겨찾기 ==========


    /**
     * 내가 좋아요한 모델 목록 조회
     */
    @GetMapping("/likes")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "좋아요한 모델 목록 조회", description = "현재 유저가 좋아요한 모델 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<PageResponse<LoraModelResponse>>> getLikedModels(
            @Parameter(hidden = true)
            Authentication authentication,
            @ParameterObject
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Long userId = getUserIdFromAuthentication(authentication);
        if (userId == null) {
            throw new IllegalArgumentException("인증 정보가 없습니다.");
        }

        PageResponse<LoraModelResponse> likedModels = likeService.getUserLikedModels(userId, pageable);

        return ResponseEntity.ok(
                ApiResponse.success("좋아요한 모델 목록 조회 성공", likedModels)
        );
    }
}
