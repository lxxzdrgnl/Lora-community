package rheon.wsd_lora_community.global.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

/**
 * 인증 관련 유틸리티 클래스
 * - OAuth2User 및 UserDetails 모두 지원
 */
public class AuthenticationUtil {

    /**
     * Authentication 객체에서 사용자 ID 추출
     * - JWT 인증 (UserDetails): username을 User ID로 사용
     * - OAuth2 인증 (OAuth2User): "id" 속성을 User ID로 사용
     *
     * @param authentication Spring Security Authentication 객체
     * @return 사용자 ID (Long)
     * @throws IllegalStateException principal 타입이 UserDetails나 OAuth2User가 아닌 경우
     */
    public static Long getUserIdFromAuthentication(Authentication authentication) {
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
     * 현재 사용자가 ADMIN 권한을 가지고 있는지 확인
     * - SecurityContextHolder에서 Authentication을 가져와 권한 확인
     *
     * @return ADMIN 권한 보유 여부
     */
    public static boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }

        return auth.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
    }
}
