package rheon.wsd_lora_community.global.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 에러 응답 형식
 */
@Getter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(
        description = "에러 응답",
        example = """
                {
                  "timestamp": "2024-01-15T14:30:00",
                  "path": "/api/models/99999",
                  "status": 404,
                  "code": "MODEL_NOT_FOUND",
                  "message": "모델을 찾을 수 없습니다."
                }
                """
)
public class ErrorResponse {

    @Schema(description = "에러 발생 시간", example = "2024-01-15T14:30:00")
    private LocalDateTime timestamp;

    @Schema(description = "요청 경로", example = "/api/models/99999")
    private String path;

    @Schema(description = "HTTP 상태 코드", example = "404")
    private int status;

    @Schema(description = "에러 코드", example = "MODEL_NOT_FOUND")
    private String code;

    @Schema(description = "에러 메시지", example = "모델을 찾을 수 없습니다.")
    private String message;

    @Schema(description = "에러 상세 정보 (선택적)")
    private Map<String, Object> details;

    @Schema(description = "필드 에러 목록 (Validation 에러 시)")
    private List<FieldError> errors;

    public ErrorResponse(String path, int status, String code, String message) {
        this.timestamp = LocalDateTime.now();
        this.path = path;
        this.status = status;
        this.code = code;
        this.message = message;
    }

    public ErrorResponse(String path, int status, String code, String message, List<FieldError> errors) {
        this.timestamp = LocalDateTime.now();
        this.path = path;
        this.status = status;
        this.code = code;
        this.message = message;
        this.errors = errors;
    }

    public ErrorResponse(String path, int status, String code, String message, Map<String, Object> details) {
        this.timestamp = LocalDateTime.now();
        this.path = path;
        this.status = status;
        this.code = code;
        this.message = message;
        this.details = details;
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
