package rheon.wsd_lora_community.global.config;

import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

/**
 * .env 파일 로드 설정
 *
 * Spring Boot 시작 시 프로젝트 루트의 .env 파일을 자동으로 로드하여
 * 시스템 환경변수로 설정합니다.
 */
@Slf4j
@Configuration
public class DotenvConfig {

    @PostConstruct
    public void loadDotenv() {
        try {
            // .env 파일 로드 (프로젝트 루트)
            Dotenv dotenv = Dotenv.configure()
                    .directory("./")  // 프로젝트 루트
                    .ignoreIfMalformed()  // 잘못된 형식 무시
                    .ignoreIfMissing()    // 파일 없으면 무시
                    .load();

            // 모든 환경변수를 시스템 속성으로 설정
            dotenv.entries().forEach(entry -> {
                String key = entry.getKey();
                String value = entry.getValue();

                // 시스템 속성으로 설정 (Spring이 읽을 수 있도록)
                System.setProperty(key, value);

                // 민감정보는 로그에 출력하지 않음
                if (key.contains("SECRET") || key.contains("PASSWORD") || key.contains("KEY")) {
                    log.info("✅ Loaded env variable: {} = ****", key);
                } else {
                    log.info("✅ Loaded env variable: {} = {}", key, value);
                }
            });

            log.info("✅ .env file loaded successfully!");

        } catch (DotenvException e) {
            log.warn("⚠️ .env file not found or malformed: {}", e.getMessage());
        } catch (Exception e) {
            log.error("❌ Failed to load .env file: {}", e.getMessage(), e);
        }
    }
}
