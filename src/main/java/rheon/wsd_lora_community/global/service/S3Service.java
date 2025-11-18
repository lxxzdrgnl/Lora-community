package rheon.wsd_lora_community.global.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;

/**
 * AWS S3 파일 관리 서비스
 *
 * - 3개 버킷 사용: training-data, models, generated-images
 * - 파일 업로드/다운로드/삭제
 * - Presigned URL 생성 (안전한 파일 접근)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class S3Service {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${aws.s3.buckets.training-data}")
    private String trainingDataBucket;

    @Value("${aws.s3.buckets.models}")
    private String modelsBucket;

    @Value("${aws.s3.buckets.generated-images}")
    private String generatedImagesBucket;

    /**
     * 버킷 타입 Enum
     */
    public enum BucketType {
        TRAINING_DATA,  // 학습 데이터셋
        MODELS,         // 학습된 모델 파일
        GENERATED_IMAGES // 생성된 이미지
    }

    /**
     * 버킷 타입에 따른 버킷 이름 반환
     */
    private String getBucketName(BucketType bucketType) {
        return switch (bucketType) {
            case TRAINING_DATA -> trainingDataBucket;
            case MODELS -> modelsBucket;
            case GENERATED_IMAGES -> generatedImagesBucket;
        };
    }

    /**
     * 파일 업로드
     *
     * @param file 업로드할 파일
     * @param bucketType 버킷 타입
     * @param s3Key S3 키 (버킷 내 경로) 예: user-123/image.png
     * @return S3 URL
     */
    public String uploadFile(MultipartFile file, BucketType bucketType, String s3Key) {
        String bucketName = getBucketName(bucketType);

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));

            log.info("File uploaded to S3: s3://{}/{}", bucketName, s3Key);
            return getPublicUrl(bucketName, s3Key);

        } catch (IOException e) {
            log.error("Failed to upload file to S3: {}", s3Key, e);
            throw new RuntimeException("S3 파일 업로드 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 바이트 배열 업로드
     *
     * @param data 업로드할 데이터
     * @param bucketType 버킷 타입
     * @param s3Key S3 키
     * @param contentType Content-Type (예: image/png)
     * @return S3 URL
     */
    public String uploadBytes(byte[] data, BucketType bucketType, String s3Key, String contentType) {
        String bucketName = getBucketName(bucketType);

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .contentType(contentType)
                    .contentLength((long) data.length)
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(data));

            log.info("Data uploaded to S3: s3://{}/{}", bucketName, s3Key);
            return getPublicUrl(bucketName, s3Key);

        } catch (Exception e) {
            log.error("Failed to upload data to S3: {}", s3Key, e);
            throw new RuntimeException("S3 데이터 업로드 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 파일 삭제
     *
     * @param bucketType 버킷 타입
     * @param s3Key S3 키
     */
    public void deleteFile(BucketType bucketType, String s3Key) {
        String bucketName = getBucketName(bucketType);

        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);

            log.info("File deleted from S3: s3://{}/{}", bucketName, s3Key);

        } catch (Exception e) {
            log.error("Failed to delete file from S3: {}", s3Key, e);
            throw new RuntimeException("S3 파일 삭제 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 파일 존재 여부 확인
     *
     * @param bucketType 버킷 타입
     * @param s3Key S3 키
     * @return 존재 여부
     */
    public boolean fileExists(BucketType bucketType, String s3Key) {
        String bucketName = getBucketName(bucketType);

        try {
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();

            s3Client.headObject(headObjectRequest);
            return true;

        } catch (NoSuchKeyException e) {
            return false;
        } catch (Exception e) {
            log.error("Failed to check file existence: {}", s3Key, e);
            return false;
        }
    }

    /**
     * Presigned URL 생성 (다운로드용)
     * - 외부에 공개하지 않고 일시적으로 파일 접근 권한 부여
     *
     * @param bucketType 버킷 타입
     * @param s3Key S3 키
     * @param duration 유효 시간
     * @return Presigned URL
     */
    public String generatePresignedGetUrl(BucketType bucketType, String s3Key, Duration duration) {
        String bucketName = getBucketName(bucketType);

        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(duration)
                    .getObjectRequest(getObjectRequest)
                    .build();

            return s3Presigner.presignGetObject(presignRequest).url().toString();

        } catch (Exception e) {
            log.error("Failed to generate presigned GET URL: {}", s3Key, e);
            throw new RuntimeException("Presigned URL 생성 실패: " + e.getMessage(), e);
        }
    }

    /**
     * Presigned URL 생성 (업로드용)
     * - 클라이언트가 직접 S3에 파일 업로드할 수 있도록 함
     *
     * @param bucketType 버킷 타입
     * @param s3Key S3 키
     * @param duration 유효 시간
     * @return Presigned URL
     */
    public String generatePresignedPutUrl(BucketType bucketType, String s3Key, Duration duration) {
        String bucketName = getBucketName(bucketType);

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();

            PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                    .signatureDuration(duration)
                    .putObjectRequest(putObjectRequest)
                    .build();

            return s3Presigner.presignPutObject(presignRequest).url().toString();

        } catch (Exception e) {
            log.error("Failed to generate presigned PUT URL: {}", s3Key, e);
            throw new RuntimeException("Presigned URL 생성 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 공개 URL 생성
     * - 버킷이 공개 상태일 때만 사용
     *
     * @param bucketName 버킷 이름
     * @param s3Key S3 키
     * @return 공개 URL
     */
    private String getPublicUrl(String bucketName, String s3Key) {
        // AWS S3 URL 형식으로 반환
        return String.format("https://%s.s3.amazonaws.com/%s",
                bucketName,
                s3Key);
    }

    /**
     * 고유한 파일명 생성
     *
     * @param originalFilename 원본 파일명
     * @return UUID가 포함된 고유 파일명
     */
    public String generateUniqueFileName(String originalFilename) {
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return UUID.randomUUID().toString() + extension;
    }

    /**
     * S3 키 생성 (경로 포함)
     *
     * @param userId 사용자 ID
     * @param filename 파일명
     * @return S3 키 (예: user-123/abc-def.png)
     */
    public String generateS3Key(Long userId, String filename) {
        return String.format("user-%d/%s", userId, filename);
    }
}
