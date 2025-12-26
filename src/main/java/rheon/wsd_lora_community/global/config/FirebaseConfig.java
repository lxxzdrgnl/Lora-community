package rheon.wsd_lora_community.global.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Firebase Admin SDK 초기화 설정
 * - 로컬/프로덕션 공통: 환경 변수 FIREBASE_CREDENTIALS에서 JSON 전체를 로드
 */
@Slf4j
@Configuration
public class FirebaseConfig {

    @Value("${firebase.project-id:}")
    private String projectId;

    @Value("${firebase.private-key-id:}")
    private String privateKeyId;

    @Value("${firebase.private-key:}")
    private String privateKey;

    @Value("${firebase.client-email:}")
    private String clientEmail;

    @Value("${firebase.client-id:}")
    private String clientId;

    @PostConstruct
    public void initialize() {
        try {
            // FirebaseApp이 이미 초기화되어 있으면 건너뛰기
            if (!FirebaseApp.getApps().isEmpty()) {
                log.info("✅ Firebase App already initialized");
                return;
            }

            // 필수 환경 변수 체크
            if (projectId == null || projectId.isEmpty() ||
                    privateKeyId == null || privateKeyId.isEmpty() ||
                    privateKey == null || privateKey.isEmpty() ||
                    clientEmail == null || clientEmail.isEmpty() ||
                    clientId == null || clientId.isEmpty()) {
                log.warn("⚠️ Firebase environment variables not set. Firebase authentication disabled.");
                return;
            }

            log.info("📄 Loading Firebase credentials from individual environment variables");

            // private_key 처리 (Base64 인코딩 여부 자동 감지)
            String formattedPrivateKey;
            if (privateKey.startsWith("-----BEGIN")) {
                // Plain text 형식 (로컬 .env)
                log.info("🔍 Plain text private key format detected");
                formattedPrivateKey = privateKey.replace("\\n", "\n");
            } else {
                // Base64 인코딩된 형식 (AWS EB)
                log.info("🔍 Base64 encoded private key detected");
                // 공백, 줄바꿈 제거 (AWS EB Console 입력 시 발생할 수 있음)
                String cleanedPrivateKey = privateKey.replaceAll("\\s+", "");
                byte[] decodedBytes = Base64.getDecoder().decode(cleanedPrivateKey);
                formattedPrivateKey = new String(decodedBytes, StandardCharsets.UTF_8);
            }

            // JSON 동적 생성 (Jackson 사용 - 이스케이프 자동 처리)
            Map<String, String> credentialsMap = new HashMap<>();
            credentialsMap.put("type", "service_account");
            credentialsMap.put("project_id", projectId);
            credentialsMap.put("private_key_id", privateKeyId);
            credentialsMap.put("private_key", formattedPrivateKey);
            credentialsMap.put("client_email", clientEmail);
            credentialsMap.put("client_id", clientId);

            com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
            String credentialsJson = objectMapper.writeValueAsString(credentialsMap);

            log.info("🔍 Constructed Firebase credentials JSON");
            log.debug("📋 Generated JSON: {}", credentialsJson);

            InputStream credentialsStream = new ByteArrayInputStream(
                    credentialsJson.getBytes(StandardCharsets.UTF_8)
            );
            GoogleCredentials credentials = GoogleCredentials.fromStream(credentialsStream);

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(credentials)
                    .build();

            FirebaseApp.initializeApp(options);
            log.info("✅ Firebase Admin SDK initialized successfully");

        } catch (Exception e) {
            log.error("❌ Failed to initialize Firebase Admin SDK: {}", e.getMessage());
            log.warn("⚠️ Firebase authentication will be disabled. App will continue without Firebase.");
            // Don't throw exception - allow app to start without Firebase
        }
    }
}
