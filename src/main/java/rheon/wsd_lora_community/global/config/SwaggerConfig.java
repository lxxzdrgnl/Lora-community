package rheon.wsd_lora_community.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import rheon.wsd_lora_community.global.dto.ErrorResponse;

import java.util.List;

/**
 * Swagger/OpenAPI 설정
 * - JWT 인증 포함
 * - API 문서 자동 생성
 * - 접속: http://localhost:8080/swagger-ui.html
 */
@Configuration
public class SwaggerConfig {

    @Value("${spring.application.name}")
    private String applicationName;

    @Bean
    public OpenAPI openAPI() {
        // JWT 보안 스키마 정의
        String jwtSchemeName = "JWT Authentication";
        SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwtSchemeName);

        Components components = new Components()
                .addSecuritySchemes(jwtSchemeName, new SecurityScheme()
                        .name(jwtSchemeName)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("JWT 토큰을 입력하세요. (Bearer 제외)"))
                // 공통 에러 응답 예시 추가
                .addExamples("BadRequestError", new Example()
                        .value("""
                                {
                                  "timestamp": "2024-01-15T14:30:00",
                                  "path": "/api/models",
                                  "status": 400,
                                  "code": "INVALID_INPUT_VALUE",
                                  "message": "잘못된 입력값입니다."
                                }
                                """))
                .addExamples("UnauthorizedError", new Example()
                        .value("""
                                {
                                  "timestamp": "2024-01-15T14:30:00",
                                  "path": "/api/models/123",
                                  "status": 401,
                                  "code": "UNAUTHORIZED",
                                  "message": "인증이 필요합니다."
                                }
                                """))
                .addExamples("ForbiddenError", new Example()
                        .value("""
                                {
                                  "timestamp": "2024-01-15T14:30:00",
                                  "path": "/api/models/123",
                                  "status": 403,
                                  "code": "HANDLE_ACCESS_DENIED",
                                  "message": "접근 권한이 없습니다."
                                }
                                """))
                .addExamples("NotFoundError", new Example()
                        .value("""
                                {
                                  "timestamp": "2024-01-15T14:30:00",
                                  "path": "/api/models/99999",
                                  "status": 404,
                                  "code": "MODEL_NOT_FOUND",
                                  "message": "모델을 찾을 수 없습니다."
                                }
                                """))
                .addExamples("ValidationError", new Example()
                        .value("""
                                {
                                  "timestamp": "2024-01-15T14:30:00",
                                  "path": "/api/users/me",
                                  "status": 422,
                                  "code": "INVALID_INPUT_VALUE",
                                  "message": "입력값 검증에 실패했습니다.",
                                  "errors": [
                                    {
                                      "field": "nickname",
                                      "value": "",
                                      "reason": "닉네임은 필수입니다."
                                    },
                                    {
                                      "field": "email",
                                      "value": "invalid-email",
                                      "reason": "올바른 이메일 형식이 아닙니다."
                                    }
                                  ]
                                }
                                """))
                .addExamples("InternalServerError", new Example()
                        .value("""
                                {
                                  "timestamp": "2024-01-15T14:30:00",
                                  "path": "/api/models",
                                  "status": 500,
                                  "code": "INTERNAL_SERVER_ERROR",
                                  "message": "서버 내부 오류가 발생했습니다."
                                }
                                """))
                // 공통 성공 응답 예시 추가
                .addExamples("SuccessResponse", new Example()
                        .value("""
                                {
                                  "success": true,
                                  "message": "요청이 성공적으로 처리되었습니다.",
                                  "data": null,
                                  "timestamp": "2024-01-15T14:30:00"
                                }
                                """))
                .addExamples("UserResponseExample", new Example()
                        .value("""
                                {
                                  "success": true,
                                  "message": "프로필 조회 성공",
                                  "data": {
                                    "id": 12345,
                                    "email": "user@example.com",
                                    "name": "홍길동",
                                    "nickname": "LoRA마스터",
                                    "profileImageUrl": "https://lh3.googleusercontent.com/a/ACg8ocK...",
                                    "role": "USER",
                                    "createdAt": "2024-01-10T10:00:00"
                                  },
                                  "timestamp": "2024-01-15T14:30:00"
                                }
                                """))
                .addExamples("ModelListResponseExample", new Example()
                        .value("""
                                {
                                  "success": true,
                                  "message": "모델 목록 조회 성공",
                                  "data": {
                                    "content": [
                                      {
                                        "id": 42,
                                        "userId": 12345,
                                        "userNickname": "LoRA마스터",
                                        "title": "소녀 전선 캐릭터 LoRA",
                                        "description": "소녀 전선 게임 캐릭터를 학습한 LoRA 모델입니다.",
                                        "isPublic": true,
                                        "viewCount": 150,
                                        "likeCount": 30,
                                        "favoriteCount": 20,
                                        "isLiked": false,
                                        "thumbnailUrl": "https://s3.amazonaws.com/bucket/sample.jpg",
                                        "s3Key": "models/42/character-model.safetensors",
                                        "fileSize": 524288000,
                                        "createdAt": "2024-01-10T10:00:00",
                                        "updatedAt": "2024-01-10T10:00:00"
                                      }
                                    ],
                                    "page": 0,
                                    "size": 20,
                                    "totalElements": 100,
                                    "totalPages": 5,
                                    "first": true,
                                    "last": false,
                                    "empty": false
                                  },
                                  "timestamp": "2024-01-15T14:30:00"
                                }
                                """))
                .addExamples("TokenResponseExample", new Example()
                        .value("""
                                {
                                  "success": true,
                                  "message": "인증에 성공했습니다.",
                                  "data": {
                                    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOjEyMzQ1LCJlbWFpbCI6InVzZXJAZXhhbXBsZS5jb20iLCJpYXQiOjE3MDUzMTI4MDAsImV4cCI6MTcwNTMxNjQwMH0.abcdefghijklmnopqrstuvwxyz123456",
                                    "refreshToken": "rt_9f8e7d6c5b4a3210fedcba0987654321",
                                    "userId": 12345,
                                    "email": "user@example.com",
                                    "nickname": "LoRA마스터",
                                    "profileImageUrl": "https://lh3.googleusercontent.com/a/ACg8ocK..."
                                  },
                                  "timestamp": "2024-01-15T14:30:00"
                                }
                                """));

        return new OpenAPI()
                .info(apiInfo())
                .servers(serverList())
                .addSecurityItem(securityRequirement)
                .components(components);
    }

    private Info apiInfo() {
        return new Info()
                .title("LoRA 모델 공유 플랫폼 API")
                .version("1.0.0")
                .description("""
                        ## LoRA 모델 학습, 생성, 공유 플랫폼 REST API 문서

                        ### 주요 기능
                        - 🔐 OAuth2 Google 로그인 (웹/모바일 지원)
                        - 🎨 LoRA 모델 학습 (FastAPI 연동, 실시간 진행률)
                        - 🖼️ 이미지 생성 (Stable Diffusion + LoRA)
                        - 💬 커뮤니티 (좋아요, 댓글, 즐겨찾기)
                        - 🔍 모델 검색 및 태그 필터링
                        - 📊 실시간 진행률 알림 (SSE)

                        ### 인증 방법
                        1. **웹 프론트엔드**: `/api/auth/google` 로 Google 로그인
                        2. **모바일 앱**: `/api/auth/google/token` 로 Google ID Token 검증
                        3. 발급받은 `accessToken`을 우측 상단 🔓 버튼에 입력
                        4. 인증이 필요한 API 호출 가능

                        ### 공통 응답 형식
                        - **성공**: `ApiResponse<T>` 형식 (success, message, data, timestamp)
                        - **에러**: `ErrorResponse` 형식 (timestamp, path, status, code, message)
                        - **페이징**: `PageResponse<T>` 형식 (content, page, size, totalElements, totalPages)

                        ### 에러 코드
                        - **400**: INVALID_INPUT_VALUE - 잘못된 입력값
                        - **401**: UNAUTHORIZED - 인증 필요
                        - **403**: HANDLE_ACCESS_DENIED - 접근 권한 없음
                        - **404**: MODEL_NOT_FOUND 등 - 리소스를 찾을 수 없음
                        - **422**: Validation Error - 입력값 검증 실패
                        - **500**: INTERNAL_SERVER_ERROR - 서버 내부 오류

                        ### 아키텍처
                        - **인증**: Stateless JWT + Refresh Token
                        - **파일 저장**: AWS S3 (Presigned URL)
                        - **실시간 알림**: SSE (Server-Sent Events)
                        - **AI 학습/생성**: FastAPI + Modal GPU
                        - **캐싱**: Redis (진행률, Pub/Sub)
                        """)
                .contact(new Contact()
                        .name("Rheon")
                        .email("pung4905@naver.com")
                        .url("https://github.com/lxxzdrgnl"));
    }

    private List<Server> serverList() {
        return List.of(
                new Server()
                        .url("https://d3ka730j70ocy8.cloudfront.net")
                        .description("프로덕션 서버"),
                new Server()
                        .url("http://localhost:5000")
                        .description("로컬 개발 서버")
        );
    }
}
