package rheon.wsd_lora_community.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

/**
 * AWS S3 / MinIO 설정
 * - endpoint가 설정되면 MinIO 모드 (path-style access)
 * - endpoint 미설정 시 AWS S3 기본 동작
 */
@Configuration
public class S3Config {

    @Value("${aws.s3.region}")
    private String region;

    @Value("${aws.credentials.access-key}")
    private String accessKey;

    @Value("${aws.credentials.secret-key}")
    private String secretKey;

    @Value("${aws.s3.endpoint:}")
    private String endpoint;          // Docker 내부 통신용 (예: http://minio:9000)

    @Value("${aws.s3.public-endpoint:}")
    private String publicEndpoint;    // 브라우저 접근용 (예: https://storage.rheon.kr)

    /**
     * S3 클라이언트 — 서버 사이드 파일 업로드/삭제에 사용
     * MinIO 환경에서는 내부 endpoint 사용
     */
    @Bean
    public S3Client s3Client() {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);

        var builder = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(true)
                        .build());

        if (endpoint != null && !endpoint.isBlank()) {
            builder.endpointOverride(URI.create(endpoint));
        }

        return builder.build();
    }

    /**
     * S3Presigner — Presigned URL 생성에 사용
     * Presigned URL은 브라우저에서 직접 접근하므로 외부 publicEndpoint 사용
     */
    @Bean
    public S3Presigner s3Presigner() {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);

        var builder = S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(true)
                        .build());

        // Presigner는 publicEndpoint 우선, 없으면 endpoint 사용
        String presignerEndpoint = (publicEndpoint != null && !publicEndpoint.isBlank())
                ? publicEndpoint
                : endpoint;

        if (presignerEndpoint != null && !presignerEndpoint.isBlank()) {
            builder.endpointOverride(URI.create(presignerEndpoint));
        }

        return builder.build();
    }
}
