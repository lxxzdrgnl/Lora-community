package rheon.wsd_lora_community.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 에러 응답 형식
 */
@Getter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "에러 응답")
public class ErrorResponse {

    @Schema(description = "에러 코드", example = "USER_NOT_FOUND")
    private String errorCode;

    @Schema(description = "에러 메시지", example = "사용자를 찾을 수 없습니다.")
    private String message;

    @Schema(description = "에러 상세 정보")
    private List<FieldError> errors;

    @Schema(description = "에러 발생 시간", example = "2025-01-15T12:00:00")
    private LocalDateTime timestamp;

    public ErrorResponse(String errorCode, String message) {
        this.errorCode = errorCode;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    public ErrorResponse(String errorCode, String message, List<FieldError> errors) {
        this.errorCode = errorCode;
        this.message = message;
        this.errors = errors;
        this.timestamp = LocalDateTime.now();
    }

    /**
     * 필드 에러 정보 (Validation 에러 등)
     */
    @Getter
    @AllArgsConstructor
    @Schema(description = "필드 에러 정보")
    public static class FieldError {
        @Schema(description = "필드명", example = "email")
        private String field;

        @Schema(description = "입력값", example = "invalid-email")
        private String value;

        @Schema(description = "에러 메시지", example = "올바른 이메일 형식이 아닙니다.")
        private String reason;
    }
}
