package rheon.wsd_lora_community.unit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import rheon.wsd_lora_community.global.exception.ErrorCode;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ErrorCode 단위 테스트
 */
@DisplayName("ErrorCode 단위 테스트")
class ErrorCodeTest {

    @Test
    @DisplayName("INVALID_INPUT_VALUE - 400 BAD REQUEST")
    void testInvalidInputValue() {
        assertThat(ErrorCode.INVALID_INPUT_VALUE.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(ErrorCode.INVALID_INPUT_VALUE.getCode()).isEqualTo("COMMON_001");
    }

    @Test
    @DisplayName("UNAUTHORIZED - 401 UNAUTHORIZED")
    void testUnauthorized() {
        assertThat(ErrorCode.UNAUTHORIZED.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(ErrorCode.UNAUTHORIZED.getCode()).isEqualTo("AUTH_001");
    }

    @Test
    @DisplayName("FORBIDDEN - 403 FORBIDDEN")
    void testForbidden() {
        assertThat(ErrorCode.FORBIDDEN.getStatus()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(ErrorCode.FORBIDDEN.getCode()).isEqualTo("COMMON_006");
    }

    @Test
    @DisplayName("RESOURCE_NOT_FOUND - 404 NOT FOUND")
    void testResourceNotFound() {
        assertThat(ErrorCode.RESOURCE_NOT_FOUND.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(ErrorCode.RESOURCE_NOT_FOUND.getCode()).isEqualTo("COMMON_007");
    }

    @Test
    @DisplayName("METHOD_NOT_ALLOWED - 405 METHOD NOT ALLOWED")
    void testMethodNotAllowed() {
        assertThat(ErrorCode.METHOD_NOT_ALLOWED.getStatus()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
        assertThat(ErrorCode.METHOD_NOT_ALLOWED.getCode()).isEqualTo("COMMON_002");
    }

    @Test
    @DisplayName("DUPLICATE_RESOURCE - 409 CONFLICT")
    void testDuplicateResource() {
        assertThat(ErrorCode.DUPLICATE_RESOURCE.getStatus()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(ErrorCode.DUPLICATE_RESOURCE.getCode()).isEqualTo("COMMON_008");
    }

    @Test
    @DisplayName("UNPROCESSABLE_ENTITY - 422 UNPROCESSABLE ENTITY")
    void testUnprocessableEntity() {
        assertThat(ErrorCode.UNPROCESSABLE_ENTITY.getStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(ErrorCode.UNPROCESSABLE_ENTITY.getCode()).isEqualTo("COMMON_009");
    }

    @Test
    @DisplayName("INTERNAL_SERVER_ERROR - 500 INTERNAL SERVER ERROR")
    void testInternalServerError() {
        assertThat(ErrorCode.INTERNAL_SERVER_ERROR.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(ErrorCode.INTERNAL_SERVER_ERROR.getCode()).isEqualTo("COMMON_003");
    }

    @Test
    @DisplayName("FASTAPI_CONNECTION_ERROR - 503 SERVICE UNAVAILABLE")
    void testFastApiConnectionError() {
        assertThat(ErrorCode.FASTAPI_CONNECTION_ERROR.getStatus()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(ErrorCode.FASTAPI_CONNECTION_ERROR.getCode()).isEqualTo("FASTAPI_001");
    }

    @Test
    @DisplayName("USER_NOT_FOUND - 404 NOT FOUND")
    void testUserNotFound() {
        assertThat(ErrorCode.USER_NOT_FOUND.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(ErrorCode.USER_NOT_FOUND.getCode()).isEqualTo("USER_001");
        assertThat(ErrorCode.USER_NOT_FOUND.getMessage()).isEqualTo("사용자를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("MODEL_NOT_FOUND - 404 NOT FOUND")
    void testModelNotFound() {
        assertThat(ErrorCode.MODEL_NOT_FOUND.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(ErrorCode.MODEL_NOT_FOUND.getCode()).isEqualTo("MODEL_001");
    }

    @Test
    @DisplayName("MODEL_NOT_OWNER - 403 FORBIDDEN")
    void testModelNotOwner() {
        assertThat(ErrorCode.MODEL_NOT_OWNER.getStatus()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(ErrorCode.MODEL_NOT_OWNER.getCode()).isEqualTo("MODEL_002");
    }

    @Test
    @DisplayName("DUPLICATE_EMAIL - 409 CONFLICT")
    void testDuplicateEmail() {
        assertThat(ErrorCode.DUPLICATE_EMAIL.getStatus()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(ErrorCode.DUPLICATE_EMAIL.getCode()).isEqualTo("USER_002");
    }

    @Test
    @DisplayName("TRAINING_JOB_NOT_FOUND - 404 NOT FOUND")
    void testTrainingJobNotFound() {
        assertThat(ErrorCode.TRAINING_JOB_NOT_FOUND.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(ErrorCode.TRAINING_JOB_NOT_FOUND.getCode()).isEqualTo("TRAINING_001");
    }

    @Test
    @DisplayName("GENERATION_FAILED - 500 INTERNAL SERVER ERROR")
    void testGenerationFailed() {
        assertThat(ErrorCode.GENERATION_FAILED.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(ErrorCode.GENERATION_FAILED.getCode()).isEqualTo("GENERATION_001");
    }

    @Test
    @DisplayName("COMMENT_NOT_FOUND - 404 NOT FOUND")
    void testCommentNotFound() {
        assertThat(ErrorCode.COMMENT_NOT_FOUND.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(ErrorCode.COMMENT_NOT_FOUND.getCode()).isEqualTo("COMMENT_001");
    }

    @Test
    @DisplayName("TAG_NOT_FOUND - 404 NOT FOUND")
    void testTagNotFound() {
        assertThat(ErrorCode.TAG_NOT_FOUND.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(ErrorCode.TAG_NOT_FOUND.getCode()).isEqualTo("TAG_001");
    }

    @Test
    @DisplayName("FILE_UPLOAD_FAILED - 500 INTERNAL SERVER ERROR")
    void testFileUploadFailed() {
        assertThat(ErrorCode.FILE_UPLOAD_FAILED.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(ErrorCode.FILE_UPLOAD_FAILED.getCode()).isEqualTo("FILE_001");
    }

    @Test
    @DisplayName("INVALID_FILE_TYPE - 400 BAD REQUEST")
    void testInvalidFileType() {
        assertThat(ErrorCode.INVALID_FILE_TYPE.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(ErrorCode.INVALID_FILE_TYPE.getCode()).isEqualTo("FILE_002");
    }

    @Test
    @DisplayName("모든 ErrorCode가 메시지를 가지고 있음")
    void testAllErrorCodesHaveMessages() {
        for (ErrorCode errorCode : ErrorCode.values()) {
            assertThat(errorCode.getMessage()).isNotNull();
            assertThat(errorCode.getMessage()).isNotEmpty();
        }
    }
}
