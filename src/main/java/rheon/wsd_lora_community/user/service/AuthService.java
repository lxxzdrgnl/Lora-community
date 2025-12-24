package rheon.wsd_lora_community.user.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import rheon.wsd_lora_community.global.exception.CustomException;
import rheon.wsd_lora_community.global.exception.ErrorCode;
import rheon.wsd_lora_community.global.security.JwtTokenProvider;
import rheon.wsd_lora_community.user.dto.TokenResponse;
import rheon.wsd_lora_community.user.entity.RefreshToken;
import rheon.wsd_lora_community.user.entity.User;
import rheon.wsd_lora_community.user.repository.RefreshTokenRepository;
import rheon.wsd_lora_community.user.repository.UserRepository;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 액세스 토큰 갱신
     */
    @Transactional
    public String refreshAccessToken(String refreshTokenValue) {
        // Refresh Token 검증
        if (!jwtTokenProvider.validateToken(refreshTokenValue)) {
            throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        // Refresh Token 조회
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new CustomException(ErrorCode.REFRESH_TOKEN_NOT_FOUND));

        // 만료 확인
        if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new CustomException(ErrorCode.REFRESH_TOKEN_EXPIRED);
        }

        // 유저 조회
        User user = refreshToken.getUser();
        if (user == null || user.isDeleted()) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }

        // 새로운 액세스 토큰 발급
        return jwtTokenProvider.createAccessToken(user.getId(), user.getEmail());
    }

    /**
     * 로그아웃 (Refresh Token 삭제)
     */
    @Transactional
    public void logout(String refreshTokenValue) {
        refreshTokenRepository.findByToken(refreshTokenValue)
                .ifPresent(refreshTokenRepository::delete);
    }

    /**
     * 유저 Refresh Token 모두 삭제
     */
    @Transactional
    public void logoutAllDevices(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        refreshTokenRepository.deleteByUser(user);
    }

    /**
     * Refresh Token 생성 및 저장
     */
    @Transactional
    public String createRefreshToken(Long userId) {
        // 유저 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // Refresh Token 생성
        String token = jwtTokenProvider.createRefreshToken(userId);
        LocalDateTime expiresAt = jwtTokenProvider.getRefreshTokenExpiryDate();

        // DB에 저장
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(token)
                .expiresAt(expiresAt)
                .build();
        refreshTokenRepository.save(refreshToken);

        return token;
    }

    /**
     * Refresh Token 저장 (OAuth2 로그인 성공 시 사용)
     */
    @Transactional
    public void saveRefreshToken(User user, String token, LocalDateTime expiresAt) {
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(token)
                .expiresAt(expiresAt)
                .build();
        refreshTokenRepository.save(refreshToken);
    }

    /**
     * Google ID Token 검증 및 JWT 토큰 발급 (모바일 앱용)
     */
    @Transactional
    public TokenResponse authenticateWithGoogleToken(String idToken) {
        try {
            log.info("🔍 Verifying Google ID Token...");

            // Google tokeninfo API 호출
            String url = "https://oauth2.googleapis.com/tokeninfo?id_token=" + idToken;
            String response = restTemplate.getForObject(url, String.class);

            if (response == null) {
                log.error("❌ Google tokeninfo API returned null");
                throw new CustomException(ErrorCode.INVALID_TOKEN);
            }

            // JSON 파싱
            JsonNode jsonNode = objectMapper.readTree(response);

            // 토큰 유효성 확인
            if (jsonNode.has("error_description")) {
                log.error("❌ Invalid Google ID Token: {}", jsonNode.get("error_description").asText());
                throw new CustomException(ErrorCode.INVALID_TOKEN);
            }

            // 사용자 정보 추출
            String email = jsonNode.get("email").asText();
            String name = jsonNode.get("name").asText();
            String providerId = jsonNode.get("sub").asText();
            String picture = jsonNode.has("picture") ? jsonNode.get("picture").asText() : null;

            log.info("✅ Google ID Token verified: email={}, name={}", email, name);

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

            // JWT 토큰 발급
            String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getEmail());
            String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());

            // Refresh Token 저장
            saveRefreshToken(user, refreshToken, jwtTokenProvider.getRefreshTokenExpiryDate());

            log.info("✅ JWT tokens issued for user: userId={}, email={}", user.getId(), user.getEmail());

            return new TokenResponse(
                    accessToken,
                    refreshToken,
                    user.getId(),
                    user.getEmail(),
                    user.getNickname(),
                    user.getProfileImageUrl()
            );

        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("❌ Failed to authenticate with Google token", e);
            throw new CustomException(ErrorCode.AUTHENTICATION_FAILED);
        }
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
                .role(User.Role.USER)  // 기본 역할은 USER
                .build();

        log.info("✅ Creating new user: email={}, nickname={}", email, nickname);
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
     * 사용자 ID로 사용자 조회
     */
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }
}
