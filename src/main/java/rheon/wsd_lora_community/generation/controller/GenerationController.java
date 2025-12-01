package rheon.wsd_lora_community.generation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import rheon.wsd_lora_community.generation.dto.AvailableModelResponse;
import rheon.wsd_lora_community.generation.dto.GenerateImageRequest;
import rheon.wsd_lora_community.generation.dto.GenerationHistoryResponse;
import rheon.wsd_lora_community.generation.service.GenerationService;
import rheon.wsd_lora_community.global.client.FastApiClient;
import rheon.wsd_lora_community.global.dto.ApiResponse;
import rheon.wsd_lora_community.global.dto.PageResponse;
import rheon.wsd_lora_community.global.service.S3UploadService;
import rheon.wsd_lora_community.global.websocket.GenerationProgressHandler;
import org.springframework.beans.factory.annotation.Value;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * 이미지 생성 컨트롤러
 * - 이미지 생성 요청 (FastAPI 연동 필요)
 * - 생성 기록 조회/관리
 * - 샘플 이미지 등록/해제
 *
 * 주의: 실제 이미지 생성은 FastAPI 서버와 통신하여 처리됩니다.
 */
@Slf4j
@RestController
@RequestMapping("/api/generate")
@RequiredArgsConstructor
@Tag(name = "Generation", description = "이미지 생성 API")
public class GenerationController {

    private final GenerationService generationService;
    private final FastApiClient fastApiClient;
    private final S3UploadService s3UploadService;
    private final GenerationProgressHandler webSocketHandler;
    private final rheon.wsd_lora_community.global.service.GpuResourceManager gpuResourceManager;

    @Value("${app.callback-url}")
    private String callbackUrlBase;

    // SSE 브로드캐스트용 Sink (완료 이벤트 전송) - 레거시, WebSocket으로 대체
    private final Sinks.Many<Map<String, Object>> completionSink = Sinks.many().multicast().onBackpressureBuffer();

    /**
     * Authentication에서 User ID 추출 (OAuth2 및 JWT 지원)
     */
    private Long getUserIdFromAuthentication(Authentication authentication) {
        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetails) {
            // JWT 인증 (UserDetails)
            UserDetails userDetails = (UserDetails) principal;
            return Long.valueOf(userDetails.getUsername());
        } else if (principal instanceof OAuth2User) {
            // OAuth2 인증
            OAuth2User oauth2User = (OAuth2User) principal;
            return Long.valueOf(oauth2User.getAttribute("id").toString());
        }

        throw new IllegalStateException("Unknown principal type: " + principal.getClass());
    }

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
            Authentication authentication,
            @Valid @RequestBody GenerateImageRequest request
    ) {
        Long userId = getUserIdFromAuthentication(authentication);

        // 생성 요청 시작 (DB에 GENERATING 상태로 저장)
        GenerationService.GenerationStartResponse startResponse =
                generationService.startGeneration(request, userId);

        // S3 URI 형식으로 전달 (Modal에서 boto3로 다운로드)
        // 형식: s3://lora-models-bucket/{userId}/{modelName}
        // 예: s3://lora-models-bucket/0/Reze
        String s3Uri = "s3://lora-models-bucket/" + startResponse.getS3Key();

        // Callback URL 설정
        String callbackUrl = callbackUrlBase + "/api/generate/history";

        // GPU 작업을 큐에 추가 (대기열 지원)
        final Long historyId = startResponse.getHistoryId();
        boolean queued = gpuResourceManager.enqueueTask(
                historyId,
                rheon.wsd_lora_community.global.service.GpuResourceManager.TaskType.GENERATION,
                () -> {
                    // GPU가 여유 생기면 자동으로 실행되는 작업
                    try {
                        // Modal API로 이미지 생성 요청 (동기)
                        String modalResponse = fastApiClient.startImageGeneration(
                                userId.toString(),
                                request.getModelId(),
                                historyId,
                                request.getPrompt(),
                                request.getNegativePrompt(),
                                s3Uri,
                                request.getNumImages() != null ? request.getNumImages() : 1,
                                request.getSteps() != null ? request.getSteps() : 40,
                                request.getGuidanceScale() != null ? request.getGuidanceScale() : 7.5,
                                request.getLoraScale(),
                                request.getSeed(),
                                request.getBaseModel() != null ? request.getBaseModel() : "stablediffusionapi/anything-v5",
                                callbackUrl
                        ).block(Duration.ofSeconds(10));

                        log.info("✅ Modal 이미지 생성 시작 성공: {}", modalResponse);

                    } catch (Exception e) {
                        // Modal 연결 실패 또는 타임아웃
                        log.error("❌ Modal 연결 실패: {}", e.getMessage());
                        e.printStackTrace();

                        // GPU 리소스 반환
                        gpuResourceManager.release(historyId);

                        // GenerationHistory를 실패 상태로 변경
                        generationService.failGeneration(historyId,
                                "Modal 서버 연결 실패: " + e.getMessage());
                    }
                }
        );

        if (!queued) {
            // 큐 추가 실패 (매우 드문 경우)
            generationService.failGeneration(startResponse.getHistoryId(), "GPU 작업 큐가 가득 찼습니다.");
            return ResponseEntity.status(503).body(
                    ApiResponse.error("GPU 작업 큐가 가득 찼습니다. 잠시 후 다시 시도해주세요.")
            );
        }

        return ResponseEntity.ok(
                ApiResponse.success(
                        "이미지 생성 요청이 FastAPI 서버로 전송되었습니다.",
                        Map.of(
                                "historyId", startResponse.getHistoryId(),
                                "message", "이미지 생성이 백그라운드에서 진행됩니다.",
                                "modelId", request.getModelId(),
                                "prompt", request.getPrompt()
                        )
                )
        );
    }

    /**
     * 이미지 생성 완료 콜백 (FastAPI 서버 전용)
     * - SUCCESS 또는 FAIL 상태로 GenerationHistory 업데이트
     */
    @PostMapping("/history")
    @Operation(
            summary = "이미지 생성 완료 콜백 (FastAPI 전용)",
            description = "FastAPI 서버에서 이미지 생성 완료 후 기록을 업데이트하기 위해 호출합니다. " +
                    "S3에 업로드된 이미지 키 목록을 전달받거나 에러 메시지를 받습니다."
    )
    public ResponseEntity<ApiResponse<?>> handleGenerationCallback(
            @RequestBody Map<String, Object> request
    ) {
        String status = (String) request.get("status");
        log.info("🔔 생성 콜백 수신: status={}, request={}", status, request);

        if ("GENERATING".equals(status)) {
            // 진행률 업데이트
            Long historyId = request.get("historyId") != null
                    ? Long.valueOf(request.get("historyId").toString())
                    : null;
            Integer currentStep = request.get("currentStep") != null
                    ? Integer.valueOf(request.get("currentStep").toString())
                    : null;
            Integer totalSteps = request.get("totalSteps") != null
                    ? Integer.valueOf(request.get("totalSteps").toString())
                    : null;
            String message = (String) request.get("message");

            if (historyId != null) {
                // DB 업데이트
                generationService.updateProgress(historyId, currentStep, totalSteps);

                // WebSocket으로 진행률 전송
                GenerationHistoryResponse history = generationService.getGenerationHistory(historyId);
                Map<String, Object> progressEvent = new HashMap<>();
                progressEvent.put("status", status);
                progressEvent.put("historyId", historyId);
                progressEvent.put("message", message);
                progressEvent.put("currentStep", currentStep);
                progressEvent.put("totalSteps", totalSteps);
                webSocketHandler.sendToUser(history.getUserId(), progressEvent);

                log.info("✅ WebSocket message sent to user {}: {}", history.getUserId(), progressEvent);
            }

            return ResponseEntity.ok(
                    ApiResponse.success("진행률 업데이트 성공", Map.of(
                            "historyId", historyId != null ? historyId : 0,
                            "message", message != null ? message : ""
                    ))
            );
        } else if ("SUCCESS".equals(status)) {
            // 성공 처리
            Long historyId = request.get("historyId") != null
                    ? Long.valueOf(request.get("historyId").toString())
                    : null;
            Long userId = request.get("userId") != null
                    ? Long.valueOf(request.get("userId").toString())
                    : null;
            Long modelId = request.get("modelId") != null
                    ? Long.valueOf(request.get("modelId").toString())
                    : null;
            String prompt = (String) request.get("prompt");
            String negativePrompt = (String) request.get("negativePrompt");
            Integer steps = request.get("steps") != null
                    ? Integer.valueOf(request.get("steps").toString())
                    : null;
            Double guidanceScale = request.get("guidanceScale") != null
                    ? Double.valueOf(request.get("guidanceScale").toString())
                    : null;
            Double loraScale = request.get("loraScale") != null
                    ? Double.valueOf(request.get("loraScale").toString())
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

            // historyId가 없으면 userId로 진행 중인 작업 찾기
            if (historyId == null && userId != null) {
                log.warn("⚠️ historyId가 없어서 userId로 진행 중인 생성 작업을 찾습니다. userId: {}", userId);
                GenerationHistoryResponse ongoingGeneration = generationService.getOngoingGeneration(userId);
                if (ongoingGeneration != null) {
                    historyId = ongoingGeneration.getId();
                    log.info("✅ 진행 중인 생성 작업 발견: historyId={}", historyId);
                } else {
                    log.error("❌ 진행 중인 생성 작업을 찾을 수 없습니다. userId: {}", userId);
                    return ResponseEntity.badRequest().body(
                            ApiResponse.error("진행 중인 생성 작업을 찾을 수 없습니다. (userId=" + userId + ")")
                    );
                }
            } else if (historyId == null) {
                log.error("❌ historyId와 userId 모두 없습니다. request: {}", request);
                return ResponseEntity.badRequest().body(
                        ApiResponse.error("생성 기록 ID 또는 사용자 ID가 필요합니다. (historyId or userId)")
                );
            }

            try {
                GenerationHistoryResponse history = generationService.completeGeneration(
                        historyId, modelId, userId, prompt, negativePrompt, steps, guidanceScale, loraScale, seed, numImages, imageS3Keys
                );
                log.info("✅ 생성 완료 처리 성공: historyId={}, images={}", historyId, imageS3Keys.size());

                // GPU 리소스 반환
                gpuResourceManager.release(historyId);

                // WebSocket으로 완료 이벤트 전송
                Map<String, Object> completionEvent = new HashMap<>();
                completionEvent.put("status", "SUCCESS");
                completionEvent.put("historyId", history.getId());
                completionEvent.put("modelId", history.getModelId());
                completionEvent.put("message", "Image generation completed");
                completionEvent.put("generatedImages", history.getGeneratedImages());

                webSocketHandler.sendToUser(history.getUserId(), completionEvent);

                // 레거시 SSE도 유지 (호환성)
                completionSink.tryEmitNext(completionEvent);

                return ResponseEntity.ok(
                        ApiResponse.success("생성 완료 처리 성공", history)
                );
            } catch (Exception e) {
                log.error("❌ 생성 완료 처리 실패: {}", e.getMessage());
                e.printStackTrace();
                return ResponseEntity.status(500).body(
                        ApiResponse.error("생성 완료 처리 실패: " + e.getMessage())
                );
            }
        } else if ("FAIL".equals(status)) {
            // 실패 처리
            Long historyId = request.get("historyId") != null
                    ? Long.valueOf(request.get("historyId").toString())
                    : null;
            Long userId = request.get("userId") != null
                    ? Long.valueOf(request.get("userId").toString())
                    : null;
            String error = (String) request.get("error");

            // historyId가 없으면 userId로 진행 중인 작업 찾기
            if (historyId == null && userId != null) {
                log.warn("⚠️ historyId가 없어서 userId로 진행 중인 생성 작업을 찾습니다. userId: {}", userId);
                GenerationHistoryResponse ongoingGeneration = generationService.getOngoingGeneration(userId);
                if (ongoingGeneration != null) {
                    historyId = ongoingGeneration.getId();
                    log.info("✅ 진행 중인 생성 작업 발견: historyId={}", historyId);
                } else {
                    log.error("❌ 진행 중인 생성 작업을 찾을 수 없습니다. userId: {}", userId);
                    return ResponseEntity.badRequest().body(
                            ApiResponse.error("진행 중인 생성 작업을 찾을 수 없습니다. (userId=" + userId + ")")
                    );
                }
            }

            if (historyId != null) {
                try {
                    generationService.failGeneration(historyId, error);
                    log.info("✅ 생성 실패 처리 완료: historyId={}, error={}", historyId, error);

                    // GPU 리소스 반환
                    gpuResourceManager.release(historyId);

                    // WebSocket으로 실패 이벤트 전송
                    GenerationHistoryResponse history = generationService.getGenerationHistory(historyId);
                    Map<String, Object> failureEvent = new HashMap<>();
                    failureEvent.put("status", "FAILED");
                    failureEvent.put("historyId", historyId);
                    failureEvent.put("message", error);

                    webSocketHandler.sendToUser(history.getUserId(), failureEvent);

                    // 레거시 SSE도 유지
                    completionSink.tryEmitNext(failureEvent);
                } catch (Exception e) {
                    log.error("❌ 생성 실패 처리 중 오류: {}", e.getMessage());
                    e.printStackTrace();
                }
            }

            return ResponseEntity.ok(
                    ApiResponse.success("생성 실패 처리 완료", Map.of("error", error != null ? error : "Unknown error"))
            );
        } else {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("잘못된 status 값: " + status));
        }
    }

    /**
     * 진행 중인 생성 작업 확인
     */
    @GetMapping("/ongoing")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "진행 중인 생성 작업 확인", description = "현재 유저의 진행 중인 이미지 생성 작업을 조회합니다. 없으면 null 반환.")
    public ResponseEntity<ApiResponse<GenerationHistoryResponse>> getOngoingGeneration(
            @Parameter(hidden = true)
            Authentication authentication
    ) {
        Long userId = getUserIdFromAuthentication(authentication);
        GenerationHistoryResponse ongoingGeneration = generationService.getOngoingGeneration(userId);

        if (ongoingGeneration != null) {
            return ResponseEntity.ok(
                    ApiResponse.success("진행 중인 생성 작업이 있습니다.", ongoingGeneration)
            );
        } else {
            return ResponseEntity.ok(
                    ApiResponse.success("진행 중인 생성 작업이 없습니다.", null)
            );
        }
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
        log.info("📊 폴링 요청: historyId={}", historyId);
        GenerationHistoryResponse history = generationService.getGenerationHistory(historyId);
        log.info("📊 폴링 응답: historyId={}, status={}, images={}",
            historyId, history.getStatus(), history.getGeneratedImages().size());

        return ResponseEntity.ok(
                ApiResponse.success("생성 기록 조회 성공", history)
        );
    }

    /**
     * 내 생성 기록 목록 조회 (모델 필터링 옵션 추가)
     */
    @GetMapping("/history/my")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "내 생성 기록 목록 조회", description = "현재 유저의 생성 기록을 조회합니다. modelId로 필터링 가능합니다.")
    public ResponseEntity<ApiResponse<PageResponse<GenerationHistoryResponse>>> getMyGenerationHistory(
            @Parameter(hidden = true)
            Authentication authentication,
            @Parameter(description = "필터링할 모델 ID (선택사항)")
            @RequestParam(required = false) Long modelId,
            @ParameterObject
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Long userId = getUserIdFromAuthentication(authentication);
        PageResponse<GenerationHistoryResponse> history = generationService.getUserGenerationHistory(userId, modelId, pageable);

        return ResponseEntity.ok(
                ApiResponse.success("생성 기록 조회 성공", history)
        );
    }

    /**
     * 사용자가 사용한 모델 목록 조회 (생성 히스토리 필터링용)
     */
    @GetMapping("/history/available-models")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "사용 가능한 모델 목록 조회", description = "현재 유저가 이미지 생성에 사용한 모델 목록을 반환합니다 (중복 제거, 최근 사용 순)")
    public ResponseEntity<ApiResponse<java.util.List<AvailableModelResponse>>> getAvailableModels(
            @Parameter(hidden = true)
            Authentication authentication
    ) {
        Long userId = getUserIdFromAuthentication(authentication);
        java.util.List<AvailableModelResponse> models = generationService.getAvailableModels(userId);

        return ResponseEntity.ok(
                ApiResponse.success("사용 가능한 모델 목록 조회 성공", models)
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
            Authentication authentication,
            @Parameter(description = "모델 ID", required = true)
            @PathVariable Long modelId,
            @ParameterObject
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Long userId = getUserIdFromAuthentication(authentication);
        PageResponse<GenerationHistoryResponse> history = generationService.getUserModelGenerationHistory(userId, modelId, pageable);

        return ResponseEntity.ok(
                ApiResponse.success("생성 기록 조회 성공", history)
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
            Authentication authentication
    ) {
        Long userId = getUserIdFromAuthentication(authentication);
        generationService.deleteGenerationHistory(historyId, userId);

        return ResponseEntity.ok(
                ApiResponse.success("생성 기록 삭제 성공")
        );
    }

    // ========== SSE 스트리밍 ==========

    /**
     * 이미지 생성 진행률 실시간 스트리밍 (SSE)
     *
     * FastAPI의 진행률 스트림 + 백엔드의 완료 이벤트를 모두 전송합니다.
     * - FastAPI: 진행률 업데이트 (IN_PROGRESS)
     * - Backend: 완료/실패 이벤트 (SUCCESS/FAILED) - 이미지 URL 포함
     *
     * 인증: EventSource는 Authorization 헤더를 지원하지 않으므로 인증 없이 모든 이벤트를 브로드캐스트합니다.
     * 클라이언트는 자신의 historyId만 필터링하여 사용해야 합니다.
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(
            summary = "이미지 생성 진행률 실시간 스트리밍 (SSE)",
            description = "FastAPI 서버의 이미지 생성 진행률과 완료 이벤트를 Server-Sent Events로 실시간 스트리밍합니다. " +
                    "EventSource API를 사용하여 연결하세요. " +
                    "진행률(IN_PROGRESS) 및 완료(SUCCESS), 실패(FAILED) 이벤트를 수신합니다. " +
                    "인증 불필요 - 모든 이벤트를 브로드캐스트하므로 클라이언트에서 historyId로 필터링하세요."
    )
    public Flux<ServerSentEvent<Map<String, Object>>> streamGenerationProgress() {
        System.out.println("🔌 SSE 클라이언트 연결됨");

        // FastAPI 진행률 스트림 (IN_PROGRESS 상태)
        Flux<Map<String, Object>> progressStream = fastApiClient.streamGenerationStatus()
                .doOnNext(status -> System.out.println("📨 FastAPI 진행률: " + status))
                .onErrorResume(error -> {
                    System.err.println("❌ FastAPI 스트림 오류: " + error.getMessage());
                    return Flux.empty();
                });

        // 백엔드 완료 이벤트 스트림 (SUCCESS/FAILED 상태)
        Flux<Map<String, Object>> completionStream = completionSink.asFlux()
                .doOnNext(event -> System.out.println("📤 완료 이벤트 브로드캐스트: " + event))
                .doOnSubscribe(sub -> System.out.println("✅ 완료 스트림 구독됨"));

        // 두 스트림 병합 (진행률 + 완료 이벤트)
        return Flux.merge(progressStream, completionStream)
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
