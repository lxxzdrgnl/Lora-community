package rheon.wsd_lora_community.global.config;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import rheon.wsd_lora_community.global.dto.ErrorResponse;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Swagger 에러 응답 예시 어노테이션
 */
public class SwaggerErrorExamples {

    /**
     * 공통 에러 응답 (401, 403, 404, 500)
     */
    @Target({ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "Bad Request",
                                    value = """
                                            {
                                              "timestamp": "2025-03-05T12:00:00",
                                              "path": "/api/models",
                                              "status": 400,
                                              "code": "COMMON_001",
                                              "message": "입력값이 올바르지 않습니다.",
                                              "errors": [
                                                {
                                                  "field": "title",
                                                  "value": "",
                                                  "reason": "제목은 필수입니다."
                                                }
                                              ]
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 필요",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "Unauthorized",
                                    value = """
                                            {
                                              "timestamp": "2025-03-05T12:00:00",
                                              "path": "/api/models",
                                              "status": 401,
                                              "code": "AUTH_001",
                                              "message": "인증이 필요합니다."
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "권한 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "Forbidden",
                                    value = """
                                            {
                                              "timestamp": "2025-03-05T12:00:00",
                                              "path": "/api/models/123",
                                              "status": 403,
                                              "code": "MODEL_002",
                                              "message": "모델 소유자만 수정/삭제할 수 있습니다."
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "리소스를 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "Not Found",
                                    value = """
                                            {
                                              "timestamp": "2025-03-05T12:00:00",
                                              "path": "/api/models/999",
                                              "status": 404,
                                              "code": "MODEL_001",
                                              "message": "모델을 찾을 수 없습니다."
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "처리할 수 없는 요청",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "Unprocessable Entity",
                                    value = """
                                            {
                                              "timestamp": "2025-03-05T12:00:00",
                                              "path": "/api/training",
                                              "status": 422,
                                              "code": "COMMON_009",
                                              "message": "요청을 처리할 수 없습니다.",
                                              "details": {
                                                "reason": "이미 학습이 진행 중입니다."
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 내부 오류",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "Internal Server Error",
                                    value = """
                                            {
                                              "timestamp": "2025-03-05T12:00:00",
                                              "path": "/api/training",
                                              "status": 500,
                                              "code": "COMMON_003",
                                              "message": "서버 내부 오류가 발생했습니다."
                                            }
                                            """
                            )
                    )
            )
    })
    public @interface CommonErrorResponses {
    }

    /**
     * 인증 필요한 엔드포인트 에러 응답
     */
    @Target({ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 필요",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "Unauthorized",
                                    value = """
                                            {
                                              "timestamp": "2025-03-05T12:00:00",
                                              "path": "/api/models",
                                              "status": 401,
                                              "code": "AUTH_001",
                                              "message": "인증이 필요합니다."
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "권한 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "Forbidden",
                                    value = """
                                            {
                                              "timestamp": "2025-03-05T12:00:00",
                                              "path": "/api/models/123",
                                              "status": 403,
                                              "code": "MODEL_002",
                                              "message": "모델 소유자만 수정/삭제할 수 있습니다."
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 내부 오류",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "Internal Server Error",
                                    value = """
                                            {
                                              "timestamp": "2025-03-05T12:00:00",
                                              "path": "/api/models",
                                              "status": 500,
                                              "code": "COMMON_003",
                                              "message": "서버 내부 오류가 발생했습니다."
                                            }
                                            """
                            )
                    )
            )
    })
    public @interface AuthRequiredErrorResponses {
    }
}
