package rheon.wsd_lora_community.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import rheon.wsd_lora_community.global.dto.ApiResponse;
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
    public ResponseEntity<ApiResponse<UserResponse>> getMyProfile(
            @Parameter(hidden = true)
            @AuthenticationPrincipal OAuth2User principal
    ) {
        Long userId = Long.valueOf(principal.getAttribute("id").toString());
        UserResponse user = userService.getUserById(userId);

        return ResponseEntity.ok(
                ApiResponse.success("프로필 조회 성공", user)
        );
    }

    /**
     * 특정 유저 프로필 조회
     */
    @GetMapping("/{userId}")
    @Operation(summary = "유저 프로필 조회", description = "특정 유저의 프로필 정보를 조회합니다.")
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
    public ResponseEntity<ApiResponse<UserResponse>> updateMyProfile(
            @Parameter(hidden = true)
            @AuthenticationPrincipal OAuth2User principal,
            @Valid @RequestBody UserUpdateRequest request
    ) {
        Long userId = Long.valueOf(principal.getAttribute("id").toString());
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
    public ResponseEntity<ApiResponse<Page<UserResponse>>> searchUsers(
            @Parameter(description = "검색할 닉네임", required = true)
            @RequestParam String nickname,
            @Parameter(description = "페이징 정보")
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
