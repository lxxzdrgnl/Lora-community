package rheon.wsd_lora_community.global.config;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import rheon.wsd_lora_community.global.dto.ErrorResponse;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 공통 에러 응답 어노테이션
 * - 401, 500 에러는 기본으로 포함
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "401",
                description = "인증 필요 (JWT 토큰 없음 또는 만료)",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponse.class),
                        examples = @ExampleObject(
                                name = "UnauthorizedError",
                                value = """
                                        {
                                          "timestamp": "2025-08-17T14:30:00",
                                          "path": "/api/models/123",
                                          "status": 401,
                                          "code": "UNAUTHORIZED",
                                          "message": "인증이 필요합니다."
                                        }
                                        """
                        )
                )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "500",
                description = "서버 내부 오류",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponse.class),
                        examples = @ExampleObject(
                                name = "InternalServerError",
                                value = """
                                        {
                                          "timestamp": "2025-08-17T14:30:00",
                                          "path": "/api/models",
                                          "status": 500,
                                          "code": "INTERNAL_SERVER_ERROR",
                                          "message": "서버 내부 오류가 발생했습니다."
                                        }
                                        """
                        )
                )
        )
})
public @interface CommonApiResponses {
}
