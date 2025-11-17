package rheon.wsd_lora_community.global.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Presigned URL 응답 DTO
 */
@Getter
@AllArgsConstructor
@Schema(description = "S3 Presigned URL 응답")
public class PresignedUrlResponse {

    @Schema(description = "Presigned URL (PUT 요청용, 유효 시간 10분)",
            example = "https://lora-training-data-bucket.s3.ap-southeast-2.amazonaws.com/users/123/uuid_file.zip?X-Amz-Algorithm=...")
    private String presignedUrl;

    @Schema(description = "파일명",
            example = "training-data.zip")
    private String fileName;

    @Schema(description = "만료 시간 (분)",
            example = "10")
    private int expiresIn;

    /**
     * Presigned URL 응답 생성
     *
     * @param presignedUrl Presigned URL
     * @param fileName 파일명
     * @return PresignedUrlResponse
     */
    public static PresignedUrlResponse of(String presignedUrl, String fileName) {
        return new PresignedUrlResponse(presignedUrl, fileName, 10);
    }
}
