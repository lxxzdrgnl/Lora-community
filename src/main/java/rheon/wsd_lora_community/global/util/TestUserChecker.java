package rheon.wsd_lora_community.global.util;

import rheon.wsd_lora_community.global.exception.CustomException;
import rheon.wsd_lora_community.global.exception.ErrorCode;

/**
 * 테스트 유저 권한 체크 유틸리티
 * 테스트 유저(ID: 100)는 읽기 및 일부 작업만 가능하도록 제한
 */
public class TestUserChecker {

    /**
     * 테스트 유저 ID
     */
    public static final Long TEST_USER_ID = 100L;

    /**
     * 테스트 유저 여부 확인
     */
    public static boolean isTestUser(Long userId) {
        return TEST_USER_ID.equals(userId);
    }

    /**
     * 테스트 유저가 수정/삭제 작업을 시도할 경우 예외 발생
     * 허용 작업: 댓글 작성/수정/삭제, 좋아요, 학습, 이미지 생성
     */
    public static void checkTestUserRestriction(Long userId, String actionDescription) {
        if (isTestUser(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN,
                    "테스트 유저는 " + actionDescription + " 작업을 수행할 수 없습니다. (읽기 전용 모드)");
        }
    }

    /**
     * 샘플 등록/해제 차단 (테스트 유저)
     */
    public static void checkSampleModification(Long userId) {
        checkTestUserRestriction(userId, "샘플 등록/해제");
    }

    /**
     * 프롬프트 수정 차단 (테스트 유저)
     */
    public static void checkPromptModification(Long userId) {
        checkTestUserRestriction(userId, "프롬프트 수정");
    }

    /**
     * 모델 수정 차단 (테스트 유저)
     */
    public static void checkModelModification(Long userId) {
        checkTestUserRestriction(userId, "모델 수정");
    }

    /**
     * 모델 삭제 차단 (테스트 유저)
     */
    public static void checkModelDeletion(Long userId) {
        checkTestUserRestriction(userId, "모델 삭제");
    }

    /**
     * 프로필 수정 차단 (테스트 유저)
     */
    public static void checkProfileModification(Long userId) {
        checkTestUserRestriction(userId, "프로필 수정");
    }
}
