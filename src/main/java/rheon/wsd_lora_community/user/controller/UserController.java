package rheon.wsd_lora_community.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import rheon.wsd_lora_community.global.dto.ApiResponse;
import rheon.wsd_lora_community.global.dto.ErrorResponse;
import rheon.wsd_lora_community.user.dto.UserResponse;
import rheon.wsd_lora_community.user.dto.UserUpdateRequest;
import rheon.wsd_lora_community.user.service.UserService;

/**
 * 유저 관리 컨트롤러
 * - 프로필 조회/수정
 * - 유저 검색
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "유저 API")
public class UserController {

    private final UserService userService;

    /**
     * 내 프로필 조회
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "내 프로필 조회", description = "현재 로그인된 유저의 프로필 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "프로필 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요 (JWT 토큰 없음 또는 만료)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "유저를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<UserResponse>> getMyProfile(
            @Parameter(hidden = true)
            Authentication authentication
    ) {
        Long userId = getUserIdFromAuthentication(authentication);
        UserResponse user = userService.getUserById(userId);

        return ResponseEntity.ok(
                ApiResponse.success("프로필 조회 성공", user)
        );
    }

    /**
     * Authentication 객체에서 사용자 ID 추출
     * OAuth2User 또는 UserDetails 모두 지원
     */
    private Long getUserIdFromAuthentication(Authentication authentication) {
        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetails) {
            // JWT 인증 (UserDetails)
            UserDetails userDetails = (UserDetails) principal;
            return Long.valueOf(userDetails.getUsername());
        } else if (principal instanceof OAuth2User) {
            // OAuth2 인증
            OAuth2User oauth2User = (OAuth2User) principal;
            return Long.valueOf(oauth2User.getAttribute("id").toString());
        }

        throw new IllegalStateException("Unknown principal type: " + principal.getClass());
    }

    /**
     * 특정 유저 프로필 조회
     */
    @GetMapping("/{userId}")
    @Operation(summary = "유저 프로필 조회", description = "특정 유저의 프로필 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "프로필 조회 성공"),
            @ApiResponse(responseCode = "404", description = "유저를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<UserResponse>> getUserProfile(
            @Parameter(description = "유저 ID", required = true)
            @PathVariable Long userId
    ) {
        UserResponse user = userService.getUserById(userId);

        return ResponseEntity.ok(
                ApiResponse.success("프로필 조회 성공", user)
        );
    }

    /**
     * 내 프로필 수정
     */
    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "내 프로필 수정", description = "현재 로그인된 유저의 프로필을 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "프로필 수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (유효성 검증 실패)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 필요 (JWT 토큰 없음 또는 만료)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "닉네임 중복",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<UserResponse>> updateMyProfile(
            @Parameter(hidden = true)
            Authentication authentication,
            @Valid @RequestBody UserUpdateRequest request
    ) {
        Long userId = getUserIdFromAuthentication(authentication);
        UserResponse updatedUser = userService.updateProfile(userId, request);

        return ResponseEntity.ok(
                ApiResponse.success("프로필 수정 성공", updatedUser)
        );
    }

    /**
     * 닉네임으로 유저 검색
     */
    @GetMapping("/search")
    @Operation(summary = "유저 검색", description = "닉네임으로 유저를 검색합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "유저 검색 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (검색 키워드 누락)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
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
     * 이메일로 유저 조회
     */
    @GetMapping("/email/{email}")
    @Operation(summary = "이메일로 유저 조회", description = "이메일 주소로 유저를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "유저 조회 성공"),
            @ApiResponse(responseCode = "404", description = "유저를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<UserResponse>> getUserByEmail(
            @Parameter(description = "이메일 주소", required = true)
            @PathVariable String email
    ) {
        UserResponse user = userService.getUserByEmail(email);

        return ResponseEntity.ok(
                ApiResponse.success("유저 조회 성공", user)
        );
    }
}
