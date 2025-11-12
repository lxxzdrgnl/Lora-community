package rheon.wsd_lora_community.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import rheon.wsd_lora_community.entity.user.RefreshToken;
import rheon.wsd_lora_community.entity.user.User;
import rheon.wsd_lora_community.repository.RefreshTokenRepository;
import rheon.wsd_lora_community.repository.UserRepository;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * OAuth2 로그인 성공 핸들러
 * - Google 로그인 성공 시 JWT 토큰 발급
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        // Google 사용자 정보 추출
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String providerId = oAuth2User.getAttribute("sub");
        String picture = oAuth2User.getAttribute("picture");

        log.info("OAuth2 로그인 성공: email={}, name={}", email, name);

        // 사용자 조회 또는 생성
        User user = userRepository.findByOauthProviderAndOauthProviderId(
                User.OAuthProvider.GOOGLE, providerId
        ).orElseGet(() -> createUser(email, name, providerId, picture));

        // JWT 토큰 생성
        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getEmail());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());

        // Refresh Token 저장
        saveRefreshToken(user, refreshToken);

        // JSON 응답 반환
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("accessToken", accessToken);
        result.put("refreshToken", refreshToken);
        result.put("user", Map.of(
                "id", user.getId(),
                "email", user.getEmail(),
                "name", user.getName(),
                "nickname", user.getNickname(),
                "profileImageUrl", user.getProfileImageUrl() != null ? user.getProfileImageUrl() : ""
        ));

        response.getWriter().write(objectMapper.writeValueAsString(result));
    }

    /**
     * 신규 사용자 생성
     */
    private User createUser(String email, String name, String providerId, String picture) {
        String nickname = generateNickname(name);

        User user = User.builder()
                .email(email)
                .name(name)
                .nickname(nickname)
                .profileImageUrl(picture)
                .oauthProvider(User.OAuthProvider.GOOGLE)
                .oauthProviderId(providerId)
                .role(User.Role.USER)
                .build();

        return userRepository.save(user);
    }

    /**
     * 닉네임 생성 (중복 시 숫자 추가)
     */
    private String generateNickname(String name) {
        String baseNickname = name;
        String nickname = baseNickname;
        int count = 1;

        while (userRepository.existsByNickname(nickname)) {
            nickname = baseNickname + count;
            count++;
        }

        return nickname;
    }

    /**
     * Refresh Token 저장
     */
    private void saveRefreshToken(User user, String refreshToken) {
        RefreshToken token = refreshTokenRepository.findByUser(user)
                .orElseGet(() -> RefreshToken.builder()
                        .user(user)
                        .token(refreshToken)
                        .expiresAt(jwtTokenProvider.getRefreshTokenExpiryDate())
                        .build());

        token.updateToken(refreshToken, jwtTokenProvider.getRefreshTokenExpiryDate());
        refreshTokenRepository.save(token);
    }
}
