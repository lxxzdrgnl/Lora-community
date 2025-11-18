package rheon.wsd_lora_community.generation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import rheon.wsd_lora_community.generation.dto.GenerateImageRequest;
import rheon.wsd_lora_community.generation.dto.GenerationHistoryResponse;
import rheon.wsd_lora_community.generation.service.GenerationService;
import rheon.wsd_lora_community.global.client.FastApiClient;
import rheon.wsd_lora_community.global.dto.ApiResponse;
import rheon.wsd_lora_community.global.dto.PageResponse;
import rheon.wsd_lora_community.global.service.S3UploadService;

import java.util.Map;

/**
 * 이미지 생성 컨트롤러
 * - 이미지 생성 요청 (FastAPI 연동 필요)
 * - 생성 기록 조회/관리
 * - 샘플 이미지 등록/해제
 *
 * 주의: 실제 이미지 생성은 FastAPI 서버와 통신하여 처리됩니다.
 */
@RestController
@RequestMapping("/api/generate")
@RequiredArgsConstructor
@Tag(name = "Generation", description = "이미지 생성 API")
public class GenerationController {

    private final GenerationService generationService;
    private final FastApiClient fastApiClient;
    private final S3UploadService s3UploadService;

    /**
     * 이미지 생성 요청 (FastAPI 연동)
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "이미지 생성 요청",
            description = "LoRA 모델을 사용하여 이미지 생성을 요청합니다. " +
                    "FastAPI 서버와 통신하여 이미지를 생성하고, 생성된 이미지 URL을 받아 기록을 저장합니다."
    )
    public ResponseEntity<ApiResponse<Map<String, Object>>> generateImage(
            @Parameter(hidden = true)
            @AuthenticationPrincipal OAuth2User principal,
            @Valid @RequestBody GenerateImageRequest request
    ) {
        Long userId = Long.valueOf(principal.getAttribute("id").toString());

        // 요청 검증 및 S3 키 조회
        String s3Key = generationService.validateGenerationRequest(request, userId);

        // S3 Presigned URL 생성 (다운로드용, 1시간 유효)
        String modelDownloadUrl = s3UploadService.generateDownloadPresignedUrl(s3Key);

        // FastAPI로 이미지 생성 요청 (비동기)
        fastApiClient.startImageGeneration(
                request.getPrompt(),
                request.getNegativePrompt(),
                modelDownloadUrl,  // Presigned URL 전달
                request.getNumImages() != null ? request.getNumImages() : 1,
                request.getSteps() != null ? request.getSteps() : 40,
                request.getGuidanceScale() != null ? request.getGuidanceScale() : 7.5,
                request.getSeed()
        ).subscribe(
                message -> {
                    // FastAPI 이미지 생성 시작 성공
                    System.out.println("FastAPI 이미지 생성 시작: " + message);
                },
                error -> {
                    // FastAPI 이미지 생성 시작 실패
                    System.err.println("FastAPI 이미지 생성 시작 실패: " + error.getMessage());
                }
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "이미지 생성 요청이 FastAPI 서버로 전송되었습니다.",
                        Map.of(
                                "message", "이미지 생성이 백그라운드에서 진행됩니다.",
                                "modelId", request.getModelId(),
                                "prompt", request.getPrompt()
                        )
                )
        );
    }

    /**
     * 이미지 생성 기록 저장 (FastAPI 서버 전용)
     * - 여러 이미지 S3 키를 받아서 GeneratedImage 엔티티로 저장
     */
    @PostMapping("/history")
    @Operation(
            summary = "이미지 생성 기록 저장 (FastAPI 전용)",
            description = "FastAPI 서버에서 이미지 생성 완료 후 기록을 저장하기 위해 호출합니다. " +
                    "S3에 업로드된 이미지 키 목록을 전달받습니다."
    )
    public ResponseEntity<ApiResponse<GenerationHistoryResponse>> saveGenerationHistory(
            @RequestBody Map<String, Object> request
    ) {
        Long modelId = Long.valueOf(request.get("modelId").toString());
        Long userId = Long.valueOf(request.get("userId").toString());
        String prompt = (String) request.get("prompt");
        String negativePrompt = (String) request.get("negativePrompt");
        Integer steps = request.get("steps") != null
                ? Integer.valueOf(request.get("steps").toString())
                : null;
        Double guidanceScale = request.get("guidanceScale") != null
                ? Double.valueOf(request.get("guidanceScale").toString())
                : null;
        Long seed = request.get("seed") != null
                ? Long.valueOf(request.get("seed").toString())
                : null;
        Integer numImages = request.get("numImages") != null
                ? Integer.valueOf(request.get("numImages").toString())
                : 1;

        // S3 키 목록 가져오기
        @SuppressWarnings("unchecked")
        java.util.List<String> imageS3Keys = (java.util.List<String>) request.get("imageS3Keys");

        GenerationHistoryResponse history = generationService.saveGenerationHistory(
                modelId, userId, prompt, negativePrompt, steps, guidanceScale, seed, numImages, imageS3Keys
        );

        return ResponseEntity.ok(
                ApiResponse.success("생성 기록 저장 성공", history)
        );
    }

    /**
     * 생성 기록 단건 조회
     */
    @GetMapping("/history/{historyId}")
    @Operation(summary = "생성 기록 조회", description = "특정 생성 기록의 상세 정보를 조회합니다.")
    public ResponseEntity<ApiResponse<GenerationHistoryResponse>> getGenerationHistory(
            @Parameter(description = "생성 기록 ID", required = true)
            @PathVariable Long historyId
    ) {
        GenerationHistoryResponse history = generationService.getGenerationHistory(historyId);

        return ResponseEntity.ok(
                ApiResponse.success("생성 기록 조회 성공", history)
        );
    }

    /**
     * 내 생성 기록 목록 조회
     */
    @GetMapping("/history/my")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "내 생성 기록 목록 조회", description = "현재 유저의 모든 생성 기록을 조회합니다.")
    public ResponseEntity<ApiResponse<PageResponse<GenerationHistoryResponse>>> getMyGenerationHistory(
            @Parameter(hidden = true)
            @AuthenticationPrincipal OAuth2User principal,
            @ParameterObject
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Long userId = Long.valueOf(principal.getAttribute("id").toString());
        PageResponse<GenerationHistoryResponse> history = generationService.getUserGenerationHistory(userId, pageable);

        return ResponseEntity.ok(
                ApiResponse.success("생성 기록 조회 성공", history)
        );
    }

    /**
     * 모델의 생성 기록 목록 조회
     */
    @GetMapping("/history/models/{modelId}")
    @Operation(summary = "모델의 생성 기록 조회", description = "특정 모델로 생성된 이미지 기록을 조회합니다.")
    public ResponseEntity<ApiResponse<PageResponse<GenerationHistoryResponse>>> getModelGenerationHistory(
            @Parameter(description = "모델 ID", required = true)
            @PathVariable Long modelId,
            @ParameterObject
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        PageResponse<GenerationHistoryResponse> history = generationService.getModelGenerationHistory(modelId, pageable);

        return ResponseEntity.ok(
                ApiResponse.success("모델 생성 기록 조회 성공", history)
        );
    }

    /**
     * 내 특정 모델 생성 기록 조회
     */
    @GetMapping("/history/my/models/{modelId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "내 특정 모델 생성 기록 조회", description = "특정 모델로 내가 생성한 이미지 기록을 조회합니다.")
    public ResponseEntity<ApiResponse<PageResponse<GenerationHistoryResponse>>> getMyModelGenerationHistory(
            @Parameter(hidden = true)
            @AuthenticationPrincipal OAuth2User principal,
            @Parameter(description = "모델 ID", required = true)
            @PathVariable Long modelId,
            @ParameterObject
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Long userId = Long.valueOf(principal.getAttribute("id").toString());
        PageResponse<GenerationHistoryResponse> history = generationService.getUserModelGenerationHistory(userId, modelId, pageable);

        return ResponseEntity.ok(
                ApiResponse.success("생성 기록 조회 성공", history)
        );
    }

    /**
     * 생성된 이미지를 샘플로 등록
     */
    @PostMapping("/images/{imageId}/sample")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "샘플로 등록", description = "생성된 이미지를 모델의 샘플 이미지로 등록합니다. (모델 소유자만 가능)")
    public ResponseEntity<ApiResponse<Void>> markImageAsSample(
            @Parameter(description = "생성 이미지 ID", required = true)
            @PathVariable Long imageId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal OAuth2User principal
    ) {
        Long userId = Long.valueOf(principal.getAttribute("id").toString());
        generationService.markImageAsSample(imageId, userId);

        return ResponseEntity.ok(
                ApiResponse.success("샘플로 등록 성공", null)
        );
    }

    /**
     * 샘플 등록 취소
     */
    @DeleteMapping("/images/{imageId}/sample")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "샘플 등록 취소", description = "샘플 이미지 등록을 취소합니다. (모델 소유자만 가능)")
    public ResponseEntity<ApiResponse<Void>> unmarkImageAsSample(
            @Parameter(description = "생성 이미지 ID", required = true)
            @PathVariable Long imageId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal OAuth2User principal
    ) {
        Long userId = Long.valueOf(principal.getAttribute("id").toString());
        generationService.unmarkImageAsSample(imageId, userId);

        return ResponseEntity.ok(
                ApiResponse.success("샘플 등록 취소 성공", null)
        );
    }

    /**
     * 생성 기록 삭제
     */
    @DeleteMapping("/history/{historyId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "생성 기록 삭제", description = "생성 기록을 삭제합니다. (생성한 유저만 가능)")
    public ResponseEntity<ApiResponse<Void>> deleteGenerationHistory(
            @Parameter(description = "생성 기록 ID", required = true)
            @PathVariable Long historyId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal OAuth2User principal
    ) {
        Long userId = Long.valueOf(principal.getAttribute("id").toString());
        generationService.deleteGenerationHistory(historyId, userId);

        return ResponseEntity.ok(
                ApiResponse.success("생성 기록 삭제 성공")
        );
    }

    // ========== SSE 스트리밍 ==========

    /**
     * 이미지 생성 진행률 실시간 스트리밍 (SSE)
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(
            summary = "이미지 생성 진행률 실시간 스트리밍 (SSE)",
            description = "FastAPI 서버의 이미지 생성 진행률을 Server-Sent Events로 실시간 스트리밍합니다. " +
                    "EventSource API를 사용하여 연결하세요."
    )
    public Flux<ServerSentEvent<Map<String, Object>>> streamGenerationProgress() {
        return fastApiClient.streamGenerationStatus()
                .map(status -> ServerSentEvent.<Map<String, Object>>builder()
                        .data(status)
                        .build())
                .doOnComplete(() -> System.out.println("생성 스트림 종료"))
                .doOnError(error -> System.err.println("생성 스트림 오류: " + error.getMessage()));
    }

    /**
     * FastAPI 서버 생성 상태 조회
     */
    @GetMapping("/fastapi/status")
    @Operation(summary = "FastAPI 생성 상태 조회", description = "FastAPI 서버의 현재 이미지 생성 상태를 조회합니다.")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getFastApiGenerationStatus() {
        return fastApiClient.getGenerationStatus()
                .map(status -> ResponseEntity.ok(
                        ApiResponse.success("FastAPI 생성 상태 조회 성공", status)
                ))
                .block(); // 블로킹 방식으로 변환 (동기 API)
    }

    /**
     * 생성된 이미지 URL 목록 조회
     */
    @GetMapping("/fastapi/images")
    @Operation(summary = "생성된 이미지 URL 조회", description = "FastAPI 서버에서 생성된 이미지 URL 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getGeneratedImages() {
        return fastApiClient.getGeneratedImageUrls()
                .map(urls -> ResponseEntity.ok(
                        ApiResponse.success("생성된 이미지 조회 성공",
                                Map.of("image_urls", urls, "count", urls.size()))
                ))
                .block(); // 블로킹 방식으로 변환 (동기 API)
    }
}
