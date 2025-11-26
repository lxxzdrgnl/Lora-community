package rheon.wsd_lora_community.global.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import rheon.wsd_lora_community.user.entity.RefreshToken;
import rheon.wsd_lora_community.user.entity.User;
import rheon.wsd_lora_community.user.repository.RefreshTokenRepository;
import rheon.wsd_lora_community.user.repository.UserRepository;

import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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

    @Value("${frontend.url:http://localhost:5173}")
    private String frontendUrl;

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
        ).orElseGet(() -> {
            return userRepository.findByEmail(email)
                    .map(existingUser -> {
                        existingUser.updateOAuthInfo(providerId, picture);
                        return userRepository.save(existingUser);
                    })
                    .orElseGet(() -> createUser(email, name, providerId, picture));
        });

        // JWT 토큰 생성
        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getEmail());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());

        // Refresh Token 저장
        saveRefreshToken(user, refreshToken);

        // state 파라미터에서 프론트엔드 URL 결정
        String targetFrontendUrl = determineTargetUrl(request);

        // 프론트엔드로 리다이렉트 (URL Fragment를 사용하여 토큰 전달)
        String redirectUrl = UriComponentsBuilder.fromUriString(targetFrontendUrl)
                .path("/auth/callback")
                .build()
                .toUriString();

        // URL Fragment에 토큰 추가 (보안상 쿼리 파라미터보다 안전)
        String fragment = String.format(
                "accessToken=%s&refreshToken=%s&userId=%d&email=%s&name=%s&nickname=%s",
                URLEncoder.encode(accessToken, StandardCharsets.UTF_8),
                URLEncoder.encode(refreshToken, StandardCharsets.UTF_8),
                user.getId(),
                URLEncoder.encode(user.getEmail(), StandardCharsets.UTF_8),
                URLEncoder.encode(user.getName(), StandardCharsets.UTF_8),
                URLEncoder.encode(user.getNickname(), StandardCharsets.UTF_8)
        );

        response.sendRedirect(redirectUrl + "#" + fragment);
    }

    private String determineTargetUrl(HttpServletRequest request) {
        String state = request.getParameter("state");
        if (state != null && state.contains("::")) {
            try {
                String[] parts = state.split("::");
                if (parts.length > 1) {
                    String encodedReferer = parts[1];
                    String decodedReferer = URLDecoder.decode(encodedReferer, StandardCharsets.UTF_8);

                    // Referer의 Origin만 추출하여 사용
                    URI uri = new URI(decodedReferer);
                    return uri.getScheme() + "://" + uri.getAuthority();
                }
            } catch (Exception e) {
                log.warn("Failed to parse state parameter for redirect URL: {}", state, e);
            }
        }
        // Fallback to default frontend URL
        return frontendUrl;
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
