package rheon.wsd_lora_community.global.config;

import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

/**
 * .env 파일 로드 설정 및 애플리케이션 시작 안내
 */
@Slf4j
@Configuration
public class DotenvConfig {

    @Value("${app.callback-url:http://localhost:8080}")
    private String callbackUrl;

    @PostConstruct
    public void loadDotenv() {
        try {
            // .env 파일 로드 (프로젝트 루트)
            Dotenv dotenv = Dotenv.configure()
                    .directory("./")  // 프로젝트 루트
                    .ignoreIfMalformed()  // 잘못된 형식 무시
                    .ignoreIfMissing()    // 파일 없으면 무시
                    .load();

            // 모든 환경변수를 시스템 속성으로 설정 (로그 출력 없음)
            dotenv.entries().forEach(entry -> {
                System.setProperty(entry.getKey(), entry.getValue());
            });

        } catch (DotenvException e) {
            log.warn("WARNING: .env file not found or malformed: {}", e.getMessage());
        } catch (Exception e) {
            log.error("ERROR: Failed to load .env file: {}", e.getMessage());
        }
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("========================================");
        log.info("Application started successfully!");
        log.info("Swagger UI: {}/swagger-ui.html", callbackUrl);
        log.info("API Docs: {}/api-docs", callbackUrl);
        log.info("Health Check: {}/actuator/health", callbackUrl);
        log.info("========================================");
    }
}
