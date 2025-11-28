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

    @Value("${aws.s3.buckets.training-data}")
    private String trainingDataBucketName;  // 학습 데이터용 버킷

    @Value("${aws.s3.buckets.models}")
    private String modelsBucketName;  // 모델 파일용 버킷

    @Value("${aws.s3.buckets.generated-images}")
    private String generatedImagesBucketName;  // 생성 이미지용 버킷

    @Value("${aws.s3.region}")
    private String region;  // S3 리전

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
                .bucket(modelsBucketName)
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
     * 다운로드용 Presigned URL 생성 (GET 요청용) - 모델 파일용
     *
     * @param s3Key S3 키 (예: models/1/my-model.safetensors)
     * @return Presigned URL (유효 시간 1시간)
     */
    public String generateDownloadPresignedUrl(String s3Key) {
        return generateDownloadPresignedUrl(s3Key, modelsBucketName);
    }

    /**
     * 학습 데이터 다운로드용 Presigned URL 생성 (GET 요청용)
     *
     * @param s3Key S3 키 (예: users/training-1-123456/image.png)
     * @return Presigned URL (유효 시간 1시간)
     */
    public String generateTrainingDataDownloadUrl(String s3Key) {
        return generateDownloadPresignedUrl(s3Key, trainingDataBucketName);
    }

    /**
     * 생성 이미지용 Public S3 URL 생성
     *
     * @param s3Key S3 키 (예: user-0/20251123_062004_1.png)
     * @return Public S3 URL (만료 시간 없음)
     */
    public String generateImageDownloadUrl(String s3Key) {
        if (s3Key == null || s3Key.isEmpty()) {
            return null;
        }

        // Public URL 형식: https://{bucket}.s3.{region}.amazonaws.com/{key}
        String publicUrl = String.format("https://%s.s3.%s.amazonaws.com/%s",
                generatedImagesBucketName, region, s3Key);

        log.debug("Public S3 URL generated for s3Key: {}, bucket: {}", s3Key, generatedImagesBucketName);

        return publicUrl;
    }

    /**
     * 다운로드용 Presigned URL 생성 (GET 요청용) - 커스텀 버킷
     *
     * @param s3Key S3 키
     * @param bucketName 버킷 이름
     * @return Presigned URL (유효 시간 1시간)
     */
    private String generateDownloadPresignedUrl(String s3Key, String bucketName) {
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

        log.debug("Download Presigned URL generated for s3Key: {}, bucket: {}", s3Key, bucketName);

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

    /**
     * 학습 데이터 업로드용 Presigned URL과 S3 Key를 함께 생성
     *
     * @param userId 사용자 ID
     * @param fileName 파일명
     * @return Map with keys: "uploadUrl", "s3Key"
     */
    public java.util.Map<String, String> generatePresignedUrlWithKey(String userId, String fileName) {
        // UUID와 파일명을 조합하여 고유한 파일명 생성
        String uniqueFileName = UUID.randomUUID().toString() + "_" + fileName;

        // S3 Key(경로) 생성: users/{userId}/{UUID}_{fileName}
        String s3Key = String.format("users/%s/%s", userId, uniqueFileName);

        // PutObjectRequest 생성 (학습 데이터는 training-data 버킷에 저장)
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(trainingDataBucketName)
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

        java.util.Map<String, String> result = new java.util.HashMap<>();
        result.put("uploadUrl", presignedUrl);
        result.put("s3Key", s3Key);
        return result;
    }
}
