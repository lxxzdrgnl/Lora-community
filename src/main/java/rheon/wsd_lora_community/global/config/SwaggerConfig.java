package rheon.wsd_lora_community.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
                        .description("JWT 토큰을 입력하세요. (Bearer 제외)"));

        return new OpenAPI()
                .info(apiInfo())
                .servers(serverList())
                .addSecurityItem(securityRequirement)
                .components(components);
    }

    private Info apiInfo() {
        return new Info()
                .title("LoRA 모델 공유 플랫폼 API")
                .description("""
                        ## LoRA 모델 학습, 생성, 공유 플랫폼 REST API 문서

                        ### 주요 기능
                        - 🔐 OAuth2 Google 로그인
                        - 🎨 LoRA 모델 학습 (FastAPI 연동)
                        - 🖼️ 이미지 생성 (실시간 진행률 추적)
                        - 💬 커뮤니티 (좋아요, 댓글, 즐겨찾기)
                        - 🔍 모델 검색 및 태그 필터링

                        ### 인증 방법
                        1. `/api/auth/google` 로 Google 로그인
                        2. 발급받은 `accessToken`을 우측 상단 🔓 버튼에 입력
                        3. 인증이 필요한 API 호출 가능

                        ### 멀티 인스턴스 고려사항
                        - Stateless JWT 기반 인증
                        - 파일 저장: S3 (프로덕션)
                        - SSE 스트림: 인스턴스별 관리
                        """)
                .contact(new Contact()
                        .name("Rheon")
                        .email("pung4905@naver.com")
                        .url("https://github.com/lxxzdrgnl"))
    }

    private List<Server> serverList() {
        return List.of(
                new Server()
                        .url("http://localhost:8080")
                        .description("로컬 개발 서버"),
                new Server()
                        .url("http://bluemingai.ap-northeast-2.elasticbeanstalk.com")
                        .description("프로덕션 서버")
        );
    }
}
