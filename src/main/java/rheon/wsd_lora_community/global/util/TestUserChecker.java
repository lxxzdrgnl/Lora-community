package rheon.wsd_lora_community.global.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import rheon.wsd_lora_community.global.exception.CustomException;
import rheon.wsd_lora_community.global.exception.ErrorCode;

/**
 * 테스트 유저 권한 체크 유틸리티
 * Role이 TEST인 유저는 읽기 및 일부 작업만 가능하도록 제한
 */
public class TestUserChecker {

    /**
     * 테스트 유저 ID (하위 호환성 유지)
     * @deprecated Role 기반 체크로 변경되었으므로 사용 불필요
     */
    @Deprecated
    public static final Long TEST_USER_ID = 100L;

    /**
     * 테스트 유저 여부 확인
     * - SecurityContextHolder에서 현재 사용자의 권한을 확인
     * - ROLE_TEST 권한을 가진 경우 true 반환
     */
    public static boolean isTestUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }

        return auth.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_TEST"));
    }

    /**
     * 테스트 유저 여부 확인 (하위 호환성 유지)
     * @deprecated isTestUser() 사용 권장
     */
    @Deprecated
    public static boolean isTestUser(Long userId) {
        return isTestUser();
    }

    /**
     * 테스트 유저가 수정/삭제 작업을 시도할 경우 예외 발생
     * 허용 작업: 댓글 작성/수정/삭제, 좋아요, 학습, 이미지 생성
     */
    public static void checkTestUserRestriction(String actionDescription) {
        if (isTestUser()) {
            throw new CustomException(ErrorCode.FORBIDDEN,
                    "테스트 유저는 " + actionDescription + " 작업을 수행할 수 없습니다. (읽기 전용 모드)");
        }
    }

    /**
     * 테스트 유저가 수정/삭제 작업을 시도할 경우 예외 발생 (하위 호환성 유지)
     * @deprecated checkTestUserRestriction(String actionDescription) 사용 권장
     */
    @Deprecated
    public static void checkTestUserRestriction(Long userId, String actionDescription) {
        checkTestUserRestriction(actionDescription);
    }

    /**
     * 샘플 등록/해제 차단 (테스트 유저)
     */
    public static void checkSampleModification(Long userId) {
        checkTestUserRestriction("샘플 등록/해제");
    }

    /**
     * 프롬프트 수정 차단 (테스트 유저)
     */
    public static void checkPromptModification(Long userId) {
        checkTestUserRestriction("프롬프트 수정");
    }

    /**
     * 모델 수정 차단 (테스트 유저)
     */
    public static void checkModelModification(Long userId) {
        checkTestUserRestriction("모델 수정");
    }

    /**
     * 모델 삭제 차단 (테스트 유저)
     */
    public static void checkModelDeletion(Long userId) {
        checkTestUserRestriction("모델 삭제");
    }

    /**
     * 프로필 수정 차단 (테스트 유저)
     */
    public static void checkProfileModification(Long userId) {
        checkTestUserRestriction("프로필 수정");
    }
}
