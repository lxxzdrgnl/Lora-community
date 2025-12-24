package rheon.wsd_lora_community.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import rheon.wsd_lora_community.global.dto.ApiResponse;
import rheon.wsd_lora_community.global.dto.ErrorResponse;
import rheon.wsd_lora_community.global.security.JwtTokenProvider;
import rheon.wsd_lora_community.user.dto.GoogleTokenRequest;
import rheon.wsd_lora_community.user.dto.TokenResponse;
import rheon.wsd_lora_community.user.service.AuthService;

import java.io.IOException;
import java.util.Map;

/**
 * 인증 관련 컨트롤러
 * - 토큰 갱신
 * - 로그아웃
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "인증 API")
public class AuthController {

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${frontend.url}")
    private String frontendUrl;

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
     * Google OAuth2 로그인 시작 (웹 프론트엔드용)
     */
    @GetMapping("/google")
    @Operation(summary = "Google 로그인", description = "Google OAuth2 로그인을 시작합니다. (웹 프론트엔드용)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "302", description = "Google 인증 페이지로 리다이렉트"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
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
     * Google ID Token 검증 및 JWT 발급 (모바일 앱용)
     */
    @PostMapping("/google/token")
    @Operation(
            summary = "Google ID Token 인증",
            description = "모바일 앱에서 받은 Google ID Token을 검증하고 JWT 토큰을 발급합니다. (모바일 앱용)"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "인증 성공, JWT 토큰 발급"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 (ID Token 검증 실패)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<TokenResponse>> authenticateWithGoogleToken(
            @Valid @RequestBody GoogleTokenRequest request
    ) {
        log.info("📱 Google ID Token authentication request received");

        TokenResponse tokenResponse = authService.authenticateWithGoogleToken(request.getIdToken());

        log.info("✅ JWT tokens issued successfully for user: {}", tokenResponse.getEmail());

        return ResponseEntity.ok(
                ApiResponse.success("인증에 성공했습니다.", tokenResponse)
        );
    }

    /**
     * 액세스 토큰 갱신
     */
    @PostMapping("/refresh")
    @Operation(summary = "액세스 토큰 갱신", description = "Refresh Token을 사용하여 새로운 Access Token을 발급받습니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "토큰 갱신 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 (Refresh Token 누락)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Refresh Token 만료 또는 유효하지 않음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
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
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그아웃 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 (Refresh Token 누락)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Refresh Token을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
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
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "모든 디바이스 로그아웃 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요 (JWT 토큰 없음 또는 만료)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
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
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "인증 정보 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요 (JWT 토큰 없음 또는 만료)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
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

    /**
     * 테스트용 JWT 토큰 발급 엔드포인트 (웹용 - 리다이렉트)
     * 테스트 유저(ID: 100)로 자동 로그인
     * 주의: 프로덕션에서는 반드시 비활성화해야 함
     */
    @GetMapping("/test")
    @Operation(summary = "테스트 로그인 (웹)", description = "테스트 유저로 자동 로그인하여 프론트엔드로 리다이렉트합니다. (개발용 - 웹용)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "302", description = "프론트엔드 콜백 페이지로 리다이렉트 (토큰 포함)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public void testLogin(HttpServletResponse response) throws IOException {
        log.info("===== GET /api/auth/test 엔드포인트 호출됨 (웹용 리다이렉트) =====");
        log.info("Frontend URL: {}", frontendUrl);

        // 테스트 유저 정보
        Long testUserId = 100L;
        String testEmail = "test@test.com";

        // JWT 토큰 생성 (Access Token + Refresh Token)
        String accessToken = jwtTokenProvider.createAccessToken(testUserId, testEmail);
        String refreshToken = authService.createRefreshToken(testUserId);

        log.info("Access Token 생성 완료: {}", accessToken.substring(0, Math.min(20, accessToken.length())) + "...");
        log.info("Refresh Token 생성 완료: {}", refreshToken.substring(0, Math.min(20, refreshToken.length())) + "...");

        // 프론트엔드 콜백 URL로 리다이렉트 (토큰을 URL Fragment에 포함)
        String redirectUrl = String.format(
                "%s/auth/callback#access_token=%s&refresh_token=%s",
                frontendUrl,
                accessToken,
                refreshToken
        );

        log.info("리다이렉트 URL: {}", redirectUrl);
        response.sendRedirect(redirectUrl);
    }

    /**
     * 테스트용 JWT 토큰 발급 엔드포인트 (모바일/API용 - JSON)
     * 테스트 유저(ID: 100)로 자동 로그인하여 JSON으로 토큰 반환
     * 주의: 프로덕션에서는 반드시 비활성화해야 함
     */
    @PostMapping("/test")
    @Operation(summary = "테스트 로그인 (모바일/API)", description = "테스트 유저로 자동 로그인하여 JWT 토큰을 JSON으로 반환합니다. (개발용 - 모바일/API용)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "토큰 발급 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<TokenResponse>> testLoginJson() {
        log.info("===== POST /api/auth/test 엔드포인트 호출됨 (모바일/API용 JSON) =====");

        // 테스트 유저 정보
        Long testUserId = 100L;
        String testEmail = "test@test.com";

        // JWT 토큰 생성 (Access Token + Refresh Token)
        String accessToken = jwtTokenProvider.createAccessToken(testUserId, testEmail);
        String refreshToken = authService.createRefreshToken(testUserId);

        log.info("Access Token 생성 완료: {}", accessToken.substring(0, Math.min(20, accessToken.length())) + "...");
        log.info("Refresh Token 생성 완료: {}", refreshToken.substring(0, Math.min(20, refreshToken.length())) + "...");

        // 사용자 정보 조회
        rheon.wsd_lora_community.user.entity.User user = authService.getUserById(testUserId);

        TokenResponse tokenResponse = new TokenResponse(
                accessToken,
                refreshToken,
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getProfileImageUrl()
        );

        return ResponseEntity.ok(
                ApiResponse.success("테스트 로그인 성공", tokenResponse)
        );
    }
}
