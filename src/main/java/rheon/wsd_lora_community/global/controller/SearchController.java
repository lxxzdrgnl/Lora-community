package rheon.wsd_lora_community.global.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import rheon.wsd_lora_community.global.dto.ApiResponse;
import rheon.wsd_lora_community.model.dto.LoraModelResponse;
import rheon.wsd_lora_community.model.service.LoraModelService;
import rheon.wsd_lora_community.user.dto.UserResponse;
import rheon.wsd_lora_community.user.service.UserService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 통합 검색 컨트롤러
 * - 모델 검색
 * - 유저 검색
 * - 태그 필터링
 * - 통합 검색
 */
@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
@Tag(name = "Search", description = "통합 검색 API")
public class SearchController {

    private final LoraModelService loraModelService;
    private final UserService userService;

    /**
     * 통합 검색 (모델 + 유저)
     */
    @GetMapping
    @Operation(
            summary = "통합 검색",
            description = "키워드로 모델과 유저를 동시에 검색합니다."
    )
    public ResponseEntity<ApiResponse<Map<String, Object>>> search(
            @Parameter(description = "검색 키워드", required = true)
            @RequestParam String query,
            @ParameterObject
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        // 모델 검색
        Page<LoraModelResponse> models = loraModelService.searchModels(query, pageable);

        // 유저 검색
        Page<UserResponse> users = userService.searchUsersByNickname(query, pageable);

        Map<String, Object> results = new HashMap<>();
        results.put("models", models);
        results.put("users", users);

        return ResponseEntity.ok(
                ApiResponse.success("통합 검색 성공", results)
        );
    }

    /**
     * 모델 검색 (제목/설명)
     */
    @GetMapping("/models")
    @Operation(
            summary = "모델 검색",
            description = "키워드로 모델의 제목 또는 설명을 검색합니다."
    )
    public ResponseEntity<ApiResponse<Page<LoraModelResponse>>> searchModels(
            @Parameter(description = "검색 키워드", required = true)
            @RequestParam String query,
            @ParameterObject
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<LoraModelResponse> models = loraModelService.searchModels(query, pageable);

        return ResponseEntity.ok(
                ApiResponse.success("모델 검색 성공", models)
        );
    }

    /**
     * 태그로 모델 필터링
     */
    @GetMapping("/models/tags")
    @Operation(
            summary = "태그로 모델 필터링",
            description = "지정한 태그를 가진 모델을 검색합니다."
    )
    public ResponseEntity<ApiResponse<Page<LoraModelResponse>>> searchModelsByTags(
            @Parameter(description = "태그 이름 목록 (쉼표로 구분)", required = true)
            @RequestParam List<String> tags,
            @ParameterObject
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<LoraModelResponse> models = loraModelService.searchModelsByTags(tags, pageable);

        return ResponseEntity.ok(
                ApiResponse.success("태그 필터링 성공", models)
        );
    }

    /**
     * 유저 검색 (닉네임)
     */
    @GetMapping("/users")
    @Operation(
            summary = "유저 검색",
            description = "닉네임으로 유저를 검색합니다."
    )
    public ResponseEntity<ApiResponse<Page<UserResponse>>> searchUsers(
            @Parameter(description = "검색할 닉네임", required = true)
            @RequestParam String nickname,
            @ParameterObject
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<UserResponse> users = userService.searchUsersByNickname(nickname, pageable);

        return ResponseEntity.ok(
                ApiResponse.success("유저 검색 성공", users)
        );
    }

    /**
     * 인기 모델 검색 (좋아요순)
     */
    @GetMapping("/models/popular")
    @Operation(
            summary = "인기 모델 조회",
            description = "좋아요가 많은 순서로 모델을 조회합니다."
    )
    public ResponseEntity<ApiResponse<Page<LoraModelResponse>>> getPopularModels(
            @Parameter(hidden = true)
            Authentication authentication,
            @ParameterObject
            @PageableDefault(size = 20, sort = "likeCount", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Long currentUserId = getUserIdFromAuthentication(authentication);
        Page<LoraModelResponse> models = loraModelService.getPopularModels(currentUserId, pageable);

        return ResponseEntity.ok(
                ApiResponse.success("인기 모델 조회 성공", models)
        );
    }

    /**
     * 최신 모델 조회
     */
    @GetMapping("/models/recent")
    @Operation(
            summary = "최신 모델 조회",
            description = "최근에 등록된 모델을 조회합니다."
    )
    public ResponseEntity<ApiResponse<Page<LoraModelResponse>>> getRecentModels(
            @Parameter(hidden = true)
            Authentication authentication,
            @ParameterObject
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Long currentUserId = getUserIdFromAuthentication(authentication);
        Page<LoraModelResponse> models = loraModelService.getPublicModels(currentUserId, pageable);

        return ResponseEntity.ok(
                ApiResponse.success("최신 모델 조회 성공", models)
        );
    }

    /**
     * 고급 검색 (복합 필터)
     */
    @PostMapping("/advanced")
    @Operation(
            summary = "고급 검색 (향후 구현)",
            description = "여러 조건을 조합하여 모델을 검색합니다. (키워드 + 태그 + 정렬 등)"
    )
    public ResponseEntity<ApiResponse<Page<LoraModelResponse>>> advancedSearch(
            @Parameter(hidden = true)
            Authentication authentication,
            @RequestBody Map<String, Object> filters,
            @ParameterObject
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        // TODO: 고급 검색 로직 구현
        // 현재는 기본 검색만 지원
        Long currentUserId = getUserIdFromAuthentication(authentication);
        String query = (String) filters.get("query");
        Page<LoraModelResponse> models = query != null
                ? loraModelService.searchModels(query, pageable)
                : loraModelService.getPublicModels(currentUserId, pageable);

        return ResponseEntity.ok(
                ApiResponse.success("고급 검색 결과", models)
        );
    }

    /**
     * Authentication에서 User ID 추출 (OAuth2 또는 JWT 지원)
     */
    private Long getUserIdFromAuthentication(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal() == null) {
            return null;
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

        return null;
    }
}
