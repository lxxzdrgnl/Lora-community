package rheon.wsd_lora_community.global.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import rheon.wsd_lora_community.global.dto.ApiResponse;
import rheon.wsd_lora_community.global.dto.PresignedUrlResponse;
import rheon.wsd_lora_community.global.service.S3UploadService;

/**
 * 파일 업로드 컨트롤러
 * - S3 Presigned URL 생성
 * - 프론트엔드에서 직접 S3에 업로드할 수 있도록 URL 제공
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Upload", description = "파일 업로드 API")
public class UploadController {

    private final S3UploadService s3UploadService;

    /**
     * S3 Presigned URL 생성
     * - 프론트엔드에서 파일명을 전달하면 S3 업로드용 Presigned URL을 생성하여 반환
     * - 생성된 URL로 PUT 요청을 보내면 S3에 파일 업로드 가능
     * - URL 유효 시간: 10분
     */
    @GetMapping("/upload-url")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "S3 Presigned URL 생성",
            description = "파일 업로드를 위한 S3 Presigned URL을 생성합니다. " +
                    "생성된 URL로 PUT 요청을 보내면 S3에 파일을 업로드할 수 있습니다. " +
                    "URL 유효 시간은 10분입니다."
    )
    public ResponseEntity<ApiResponse<PresignedUrlResponse>> getPresignedUrl(
            @Parameter(description = "업로드할 파일명", required = true, example = "training-data.zip")
            @RequestParam String fileName,
            @Parameter(hidden = true)
            @AuthenticationPrincipal OAuth2User principal
    ) {
        // 현재 인증된 유저 ID 추출
        String userId = principal.getAttribute("id").toString();

        // Presigned URL 생성
        String presignedUrl = s3UploadService.generatePresignedUrl(userId, fileName);

        // 응답 DTO 생성
        PresignedUrlResponse response = PresignedUrlResponse.of(presignedUrl, fileName);

        return ResponseEntity.ok(
                ApiResponse.success("Presigned URL 생성 성공", response)
        );
    }
}
