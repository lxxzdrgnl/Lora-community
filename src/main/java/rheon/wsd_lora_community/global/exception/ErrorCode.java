package rheon.wsd_lora_community.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 에러 코드 정의
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 공통 에러
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "COMMON_001", "입력값이 올바르지 않습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "COMMON_002", "지원하지 않는 HTTP 메서드입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_003", "서버 내부 오류가 발생했습니다."),
    INVALID_TYPE_VALUE(HttpStatus.BAD_REQUEST, "COMMON_004", "입력 타입이 올바르지 않습니다."),
    HANDLE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "COMMON_005", "접근 권한이 없습니다."),

    // 인증 에러
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AUTH_001", "인증이 필요합니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_002", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_003", "만료된 토큰입니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_004", "유효하지 않은 리프레시 토큰입니다."),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "AUTH_005", "리프레시 토큰을 찾을 수 없습니다."),
    REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "AUTH_006", "리프레시 토큰이 만료되었습니다."),

    // 유저 에러
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_001", "사용자를 찾을 수 없습니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "USER_002", "이미 존재하는 이메일입니다."),
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "USER_003", "이미 존재하는 닉네임입니다."),
    NICKNAME_ALREADY_EXISTS(HttpStatus.CONFLICT, "USER_004", "이미 사용 중인 닉네임입니다."),

    // LoRA 모델 에러
    MODEL_NOT_FOUND(HttpStatus.NOT_FOUND, "MODEL_001", "모델을 찾을 수 없습니다."),
    MODEL_NOT_OWNER(HttpStatus.FORBIDDEN, "MODEL_002", "모델 소유자만 수정/삭제할 수 있습니다."),
    MODEL_NOT_PUBLIC(HttpStatus.FORBIDDEN, "MODEL_003", "비공개 모델입니다."),
    MODEL_TRAINING_IN_PROGRESS(HttpStatus.CONFLICT, "MODEL_004", "모델이 학습 중입니다."),

    // 샘플 이미지 에러
    SAMPLE_NOT_FOUND(HttpStatus.NOT_FOUND, "SAMPLE_001", "샘플 이미지를 찾을 수 없습니다."),
    SAMPLE_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "SAMPLE_002", "샘플 이미지는 최대 10개까지 등록할 수 있습니다."),

    // 프롬프트 에러
    PROMPT_NOT_FOUND(HttpStatus.NOT_FOUND, "PROMPT_001", "프롬프트를 찾을 수 없습니다."),

    // 태그 에러
    TAG_NOT_FOUND(HttpStatus.NOT_FOUND, "TAG_001", "태그를 찾을 수 없습니다."),
    DUPLICATE_TAG(HttpStatus.CONFLICT, "TAG_002", "이미 존재하는 태그입니다."),

    // 학습 작업 에러
    TRAINING_JOB_NOT_FOUND(HttpStatus.NOT_FOUND, "TRAINING_001", "학습 작업을 찾을 수 없습니다."),
    TRAINING_ALREADY_IN_PROGRESS(HttpStatus.CONFLICT, "TRAINING_002", "이미 학습이 진행 중입니다."),
    TRAINING_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "TRAINING_003", "학습에 실패했습니다."),

    // 이미지 생성 에러
    GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "GENERATION_001", "이미지 생성에 실패했습니다."),
    GENERATION_NOT_FOUND(HttpStatus.NOT_FOUND, "GENERATION_002", "생성 기록을 찾을 수 없습니다."),

    // 댓글 에러
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMENT_001", "댓글을 찾을 수 없습니다."),
    COMMENT_NOT_OWNER(HttpStatus.FORBIDDEN, "COMMENT_002", "댓글 작성자만 수정/삭제할 수 있습니다."),
    PARENT_COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMENT_003", "부모 댓글을 찾을 수 없습니다."),

    // 파일 에러
    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "FILE_001", "파일 업로드에 실패했습니다."),
    INVALID_FILE_TYPE(HttpStatus.BAD_REQUEST, "FILE_002", "지원하지 않는 파일 형식입니다."),
    FILE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "FILE_003", "파일 크기가 제한을 초과했습니다."),

    // FastAPI 연동 에러
    FASTAPI_CONNECTION_ERROR(HttpStatus.SERVICE_UNAVAILABLE, "FASTAPI_001", "FastAPI 서버에 연결할 수 없습니다."),
    FASTAPI_REQUEST_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "FASTAPI_002", "FastAPI 요청에 실패했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
