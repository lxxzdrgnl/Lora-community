package rheon.wsd_lora_community.model.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import rheon.wsd_lora_community.global.dto.ApiResponse;
import rheon.wsd_lora_community.global.dto.ErrorResponse;
import rheon.wsd_lora_community.global.exception.CustomException;
import rheon.wsd_lora_community.global.exception.ErrorCode;
import rheon.wsd_lora_community.model.dto.TagResponse;
import rheon.wsd_lora_community.model.service.TagService;

import java.util.List;
import java.util.Map;

/**
 * 태그 관리 컨트롤러
 * - 태그 조회/검색
 * - 모델-태그 연결 관리
 */
@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
@Tag(name = "Tag", description = "태그 API")
public class TagController {

    private final TagService tagService;

    /**
     * Authentication 객체에서 사용자 ID 추출
     * OAuth2User 또는 UserDetails 모두 지원
     */
    private Long getUserIdFromAuthentication(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
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
            return Long.valueOf(userDetails.getUsername()); // username에 userId 저장됨
        }

        throw new CustomException(ErrorCode.UNAUTHORIZED);
    }

    /**
     * 모든 태그 조회
     */
    @GetMapping
    @Operation(summary = "모든 태그 조회", description = "등록된 모든 태그를 조회합니다.")
    public ResponseEntity<ApiResponse<List<TagResponse>>> getAllTags() {
        List<TagResponse> tags = tagService.getAllTags();

        return ResponseEntity.ok(
                ApiResponse.success("태그 목록 조회 성공", tags)
        );
    }

    /**
     * 인기 태그 조회 (Top 20)
     */
    @GetMapping("/popular")
    @Operation(summary = "인기 태그 조회", description = "사용 횟수가 많은 상위 20개 태그를 조회합니다.")
    public ResponseEntity<ApiResponse<List<TagResponse>>> getPopularTags() {
        List<TagResponse> tags = tagService.getPopularTags();

        return ResponseEntity.ok(
                ApiResponse.success("인기 태그 조회 성공", tags)
        );
    }

    /**
     * 카테고리별 태그 조회
     */
    @GetMapping("/category/{category}")
    @Operation(summary = "카테고리별 태그 조회", description = "특정 카테고리의 태그를 조회합니다.")
    public ResponseEntity<ApiResponse<List<TagResponse>>> getTagsByCategory(
            @Parameter(description = "태그 카테고리 (CHARACTER, STYLE, THEME, OTHER)", required = true)
            @PathVariable rheon.wsd_lora_community.model.entity.Tag.TagCategory category
    ) {
        List<TagResponse> tags = tagService.getTagsByCategory(category);

        return ResponseEntity.ok(
                ApiResponse.success("카테고리별 태그 조회 성공", tags)
        );
    }

    /**
     * 태그 검색
     */
    @GetMapping("/search")
    @Operation(summary = "태그 검색", description = "태그 이름으로 검색합니다.")
    public ResponseEntity<ApiResponse<List<TagResponse>>> searchTags(
            @Parameter(description = "검색 키워드", required = true)
            @RequestParam String keyword
    ) {
        List<TagResponse> tags = tagService.searchTags(keyword);

        return ResponseEntity.ok(
                ApiResponse.success("태그 검색 성공", tags)
        );
    }

    /**
     * 태그 상세 조회
     */
    @GetMapping("/{tagId}")
    @Operation(summary = "태그 상세 조회", description = "특정 태그의 상세 정보를 조회합니다.")
    public ResponseEntity<ApiResponse<TagResponse>> getTag(
            @Parameter(description = "태그 ID", required = true)
            @PathVariable Long tagId
    ) {
        TagResponse tag = tagService.getTag(tagId);

        return ResponseEntity.ok(
                ApiResponse.success("태그 조회 성공", tag)
        );
    }

    /**
     * 모델에 태그 추가
     */
    @PostMapping("/models/{modelId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "모델에 태그 추가", description = "모델에 태그를 추가합니다. (모델 소유자만 가능)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "태그 추가 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음 (소유자만 가능)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "모델을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<Void>> addTagToModel(
            @Parameter(description = "모델 ID", required = true)
            @PathVariable Long modelId,
            @Parameter(hidden = true)
            Authentication authentication,
            @RequestBody Map<String, Object> request
    ) {
        Long userId = getUserIdFromAuthentication(authentication);
        String tagName = (String) request.get("tagName");
        String categoryStr = (String) request.get("category");

        rheon.wsd_lora_community.model.entity.Tag.TagCategory category = null;
        if (categoryStr != null) {
            category = rheon.wsd_lora_community.model.entity.Tag.TagCategory.valueOf(categoryStr);
        }

        tagService.addTagToModel(modelId, tagName, category, userId);

        return ResponseEntity.ok(
                ApiResponse.success("태그 추가 성공")
        );
    }

    /**
     * 모델에서 태그 제거
     */
    @DeleteMapping("/models/{modelId}/tags/{tagId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "모델에서 태그 제거", description = "모델에서 태그를 제거합니다. (모델 소유자만 가능)")
    public ResponseEntity<ApiResponse<Void>> removeTagFromModel(
            @Parameter(description = "모델 ID", required = true)
            @PathVariable Long modelId,
            @Parameter(description = "태그 ID", required = true)
            @PathVariable Long tagId,
            @Parameter(hidden = true)
            Authentication authentication
    ) {
        Long userId = getUserIdFromAuthentication(authentication);
        tagService.removeTagFromModel(modelId, tagId, userId);

        return ResponseEntity.ok(
                ApiResponse.success("태그 제거 성공")
        );
    }

    /**
     * 모델의 태그 목록 조회
     */
    @GetMapping("/models/{modelId}")
    @Operation(summary = "모델의 태그 목록 조회", description = "특정 모델에 연결된 태그 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<List<TagResponse>>> getModelTags(
            @Parameter(description = "모델 ID", required = true)
            @PathVariable Long modelId
    ) {
        List<TagResponse> tags = tagService.getTagsByModel(modelId);

        return ResponseEntity.ok(
                ApiResponse.success("모델 태그 조회 성공", tags)
        );
    }

    /**
     * 태그 카테고리 수정 (관리자용)
     */
    @PutMapping("/{tagId}/category")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "태그 카테고리 수정 (관리자)", description = "태그의 카테고리를 수정합니다. (관리자만 가능)")
    public ResponseEntity<ApiResponse<TagResponse>> updateTagCategory(
            @Parameter(description = "태그 ID", required = true)
            @PathVariable Long tagId,
            @RequestBody Map<String, String> request
    ) {
        String categoryStr = request.get("category");
        rheon.wsd_lora_community.model.entity.Tag.TagCategory category =
                rheon.wsd_lora_community.model.entity.Tag.TagCategory.valueOf(categoryStr);

        TagResponse updatedTag = tagService.updateTagCategory(tagId, category);

        return ResponseEntity.ok(
                ApiResponse.success("태그 카테고리 수정 성공", updatedTag)
        );
    }
}
