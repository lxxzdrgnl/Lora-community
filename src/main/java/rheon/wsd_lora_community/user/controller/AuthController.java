package rheon.wsd_lora_community.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import rheon.wsd_lora_community.global.dto.ApiResponse;
import rheon.wsd_lora_community.user.service.AuthService;

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
            @AuthenticationPrincipal OAuth2User principal
    ) {
        Long userId = Long.valueOf(principal.getAttribute("id").toString());
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
            @AuthenticationPrincipal OAuth2User principal
    ) {
        return ResponseEntity.ok(
                ApiResponse.success("인증 정보 조회 성공",
                        principal.getAttributes())
        );
    }
}
