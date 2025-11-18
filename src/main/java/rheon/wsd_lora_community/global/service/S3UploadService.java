package rheon.wsd_lora_community.global.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.UUID;

/**
 * S3 업로드 서비스
 * - Presigned URL 생성
 * - 프론트엔드에서 직접 S3에 업로드할 수 있도록 URL 제공
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class S3UploadService {

    private final S3Presigner s3Presigner;

    @Value("${aws.s3.buckets.models}")
    private String bucketName;  // 기본 버킷 (모델 파일용)

    /**
     * Presigned URL 생성
     *
     * @param userId 사용자 ID
     * @param fileName 파일명
     * @return Presigned URL (PUT 요청용, 유효 시간 10분)
     */
    public String generatePresignedUrl(String userId, String fileName) {
        // UUID와 파일명을 조합하여 고유한 파일명 생성
        String uniqueFileName = UUID.randomUUID().toString() + "_" + fileName;

        // S3 Key(경로) 생성: users/{userId}/{UUID}_{fileName}
        String s3Key = String.format("users/%s/%s", userId, uniqueFileName);

        // PutObjectRequest 생성
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .build();

        // PutObjectPresignRequest 생성 (유효 시간 10분)
        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(10))
                .putObjectRequest(putObjectRequest)
                .build();

        // Presigned URL 생성
        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);
        String presignedUrl = presignedRequest.url().toString();

        log.info("Presigned URL 생성 완료 - userId: {}, fileName: {}, s3Key: {}", userId, fileName, s3Key);

        return presignedUrl;
    }

    /**
     * 다운로드용 Presigned URL 생성 (GET 요청용)
     *
     * @param s3Key S3 키 (예: models/1/my-model.safetensors)
     * @return Presigned URL (유효 시간 1시간)
     */
    public String generateDownloadPresignedUrl(String s3Key) {
        if (s3Key == null || s3Key.isEmpty()) {
            return null;
        }

        // GetObjectRequest 생성
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .build();

        // GetObjectPresignRequest 생성 (유효 시간 1시간)
        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofHours(1))
                .getObjectRequest(getObjectRequest)
                .build();

        // Presigned URL 생성
        PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
        String presignedUrl = presignedRequest.url().toString();

        log.debug("Download Presigned URL generated for s3Key: {}", s3Key);

        return presignedUrl;
    }

    /**
     * S3 Key(경로) 생성
     *
     * @param userId 사용자 ID
     * @param fileName 파일명
     * @return S3 Key (users/{userId}/{UUID}_{fileName})
     */
    public String generateS3Key(String userId, String fileName) {
        String uniqueFileName = UUID.randomUUID().toString() + "_" + fileName;
        return String.format("users/%s/%s", userId, uniqueFileName);
    }
}
