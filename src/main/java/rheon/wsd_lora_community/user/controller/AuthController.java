package rheon.wsd_lora_community.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import rheon.wsd_lora_community.global.dto.ApiResponse;
import rheon.wsd_lora_community.user.service.AuthService;

import java.io.IOException;
import java.util.Map;

/**
 * 인증 관련 컨트롤러
 * - 토큰 갱신
 * - 로그아웃
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "인증 API")
public class AuthController {

    private final AuthService authService;

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
     * Google OAuth2 로그인 시작
     */
    @GetMapping("/google")
    @Operation(summary = "Google 로그인", description = "Google OAuth2 로그인을 시작합니다.")
    public void googleLogin(
            @RequestParam(required = false) String prompt,
            HttpServletResponse response
    ) throws IOException {
        // prompt 파라미터를 포함하여 리다이렉트 (모바일 지원)
        String redirectUrl = "/oauth2/authorization/google";
        if (prompt != null && !prompt.isEmpty()) {
            redirectUrl += "?prompt=" + prompt;
        }
        response.sendRedirect(redirectUrl);
    }

    /**
     * 액세스 토큰 갱신
     */
    @PostMapping("/refresh")
    @Operation(summary = "액세스 토큰 갱신", description = "Refresh Token을 사용하여 새로운 Access Token을 발급받습니다.")
    public ResponseEntity<ApiResponse<Map<String, String>>> refreshAccessToken(
            @Parameter(description = "Refresh Token", required = true)
            @RequestBody Map<String, String> request
    ) {
        String refreshToken = request.get("refreshToken");
        String newAccessToken = authService.refreshAccessToken(refreshToken);

        return ResponseEntity.ok(
                ApiResponse.success("토큰이 갱신되었습니다.",
                        Map.of("accessToken", newAccessToken))
        );
    }

    /**
     * 로그아웃 (단일 디바이스)
     */
    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "현재 Refresh Token을 삭제하여 로그아웃합니다.")
    public ResponseEntity<ApiResponse<Void>> logout(
            @Parameter(description = "Refresh Token", required = true)
            @RequestBody Map<String, String> request
    ) {
        String refreshToken = request.get("refreshToken");
        authService.logout(refreshToken);

        return ResponseEntity.ok(
                ApiResponse.success("로그아웃되었습니다.")
        );
    }

    /**
     * 모든 디바이스에서 로그아웃
     */
    @PostMapping("/logout-all")
    @Operation(summary = "모든 디바이스 로그아웃", description = "유저의 모든 Refresh Token을 삭제하여 모든 디바이스에서 로그아웃합니다.")
    public ResponseEntity<ApiResponse<Void>> logoutAllDevices(
            @Parameter(hidden = true)
            Authentication authentication
    ) {
        Long userId = getUserIdFromAuthentication(authentication);
        authService.logoutAllDevices(userId);

        return ResponseEntity.ok(
                ApiResponse.success("모든 디바이스에서 로그아웃되었습니다.")
        );
    }

    /**
     * 현재 인증 정보 확인 (테스트용)
     */
    @GetMapping("/me")
    @Operation(summary = "현재 인증 정보 확인", description = "현재 로그인된 유저의 정보를 확인합니다.")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCurrentUser(
            @Parameter(hidden = true)
            Authentication authentication
    ) {
        Long userId = getUserIdFromAuthentication(authentication);
        Object principal = authentication.getPrincipal();

        Map<String, Object> userInfo = new java.util.HashMap<>();
        userInfo.put("userId", userId);

        if (principal instanceof OAuth2User) {
            OAuth2User oauth2User = (OAuth2User) principal;
            userInfo.putAll(oauth2User.getAttributes());
        } else if (principal instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) principal;
            userInfo.put("username", userDetails.getUsername());
            userInfo.put("authorities", userDetails.getAuthorities());
        }

        return ResponseEntity.ok(
                ApiResponse.success("인증 정보 조회 성공", userInfo)
        );
    }
}
