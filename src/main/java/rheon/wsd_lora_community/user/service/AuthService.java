package rheon.wsd_lora_community.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rheon.wsd_lora_community.global.exception.CustomException;
import rheon.wsd_lora_community.global.exception.ErrorCode;
import rheon.wsd_lora_community.global.security.JwtTokenProvider;
import rheon.wsd_lora_community.user.entity.RefreshToken;
import rheon.wsd_lora_community.user.entity.User;
import rheon.wsd_lora_community.user.repository.RefreshTokenRepository;
import rheon.wsd_lora_community.user.repository.UserRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;

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
}
