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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import rheon.wsd_lora_community.generation.dto.AvailableModelResponse;
import rheon.wsd_lora_community.generation.dto.GenerateImageRequest;
import rheon.wsd_lora_community.generation.dto.GenerationHistoryResponse;
import rheon.wsd_lora_community.generation.service.GenerationService;
import rheon.wsd_lora_community.global.client.FastApiClient;
import rheon.wsd_lora_community.global.dto.ApiResponse;
import rheon.wsd_lora_community.global.dto.PageResponse;
import rheon.wsd_lora_community.global.queue.RedisQueueService;
import rheon.wsd_lora_community.global.queue.RedisQueueService.JobType;
import rheon.wsd_lora_community.global.service.JobCallbackService;
import rheon.wsd_lora_community.global.service.S3UploadService;
import rheon.wsd_lora_community.global.util.AuthenticationUtil;
import rheon.wsd_lora_community.global.sse.SseEmitterService;
import rheon.wsd_lora_community.global.exception.CustomException;
import rheon.wsd_lora_community.global.exception.ErrorCode;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
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
    private final JobCallbackService jobCallbackService;
    private final RedisQueueService queueService;
    private final org.springframework.data.redis.core.RedisTemplate<String, Object> redisTemplate;
    private final SseEmitterService sseEmitterService;

    @Value("${app.callback-url}")
    private String callbackUrlBase;


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
        Long userId = AuthenticationUtil.getUserIdFromAuthentication(authentication);

        // 생성 요청 시작 (DB에 GENERATING 상태로 저장)
        GenerationService.GenerationStartResponse startResponse =
                generationService.startGeneration(request, userId);

        // Redis 큐에 작업 추가 (JobQueueWorker가 자동으로 처리)
        Map<String, Object> jobData = new HashMap<>();
        jobData.put("userId", userId);
        jobData.put("modelId", request.getModelId());
        jobData.put("modelPath", startResponse.getS3Key());
        jobData.put("prompt", request.getPrompt());
        jobData.put("negativePrompt", request.getNegativePrompt());
        jobData.put("steps", request.getSteps() != null ? request.getSteps() : 40);
        jobData.put("guidanceScale", request.getGuidanceScale() != null ? request.getGuidanceScale() : 7.5);
        jobData.put("loraScale", request.getLoraScale());
        jobData.put("seed", request.getSeed());
        jobData.put("numImages", request.getNumImages() != null ? request.getNumImages() : 1);

        queueService.enqueue(JobType.GENERATION, startResponse.getHistoryId(), jobData);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "이미지 생성 작업이 큐에 추가되었습니다. 순차적으로 처리됩니다.",
                        Map.of(
                                "historyId", startResponse.getHistoryId(),
                                "message", "이미지 생성이 대기열에 추가되었습니다.",
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

            jobCallbackService.handleGenerationProgress(historyId, currentStep, totalSteps, message);

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

            try {
                GenerationHistoryResponse history = jobCallbackService.handleGenerationSuccess(
                        historyId, userId, modelId, prompt, negativePrompt, steps, guidanceScale, loraScale, seed, numImages, imageS3Keys
                );
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

            jobCallbackService.handleGenerationFailure(historyId, userId, error);

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
        Long userId = AuthenticationUtil.getUserIdFromAuthentication(authentication);
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
     * 이미지 생성 진행률 조회 (Redis 캐시에서)
     * - 새로고침 시 진행률 복원용
     * - Redis에 TTL 1시간으로 저장된 진행 상황 반환
     */
    @GetMapping("/history/{historyId}/progress")
    @Operation(
            summary = "이미지 생성 진행률 조회 (캐시)",
            description = "Redis 캐시에 저장된 이미지 생성 진행 상황을 조회합니다. " +
                    "새로고침 후에도 진행률을 유지하기 위해 사용됩니다. " +
                    "TTL 1시간 동안 유지되며, 없으면 null 반환합니다."
    )
    public ResponseEntity<ApiResponse<Map<String, Object>>> getGenerationProgress(
            @Parameter(description = "생성 기록 ID", required = true)
            @PathVariable Long historyId
    ) {
        String redisKey = "generation:progress:" + historyId;
        Object cachedProgress = redisTemplate.opsForValue().get(redisKey);

        if (cachedProgress != null) {
            @SuppressWarnings("unchecked")
            Map<String, Object> progressData = (Map<String, Object>) cachedProgress;

            return ResponseEntity.ok(
                    ApiResponse.success("생성 진행률 조회 성공 (캐시)", progressData)
            );
        } else {
            return ResponseEntity.ok(
                    ApiResponse.success("캐시된 진행률 없음", null)
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
        Long userId = AuthenticationUtil.getUserIdFromAuthentication(authentication);
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
        Long userId = AuthenticationUtil.getUserIdFromAuthentication(authentication);
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
        Long userId = AuthenticationUtil.getUserIdFromAuthentication(authentication);
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
        Long userId = AuthenticationUtil.getUserIdFromAuthentication(authentication);
        generationService.deleteGenerationHistory(historyId, userId);

        return ResponseEntity.ok(
                ApiResponse.success("생성 기록 삭제 성공")
        );
    }

    // ========== SSE (Server-Sent Events) 엔드포인트 ==========

    /**
     * SSE 연결 (이미지 생성 진행률 실시간 스트리밍)
     * - Redis Pub/Sub 메시지를 SSE로 전송
     * - 새로고침 시 자동 재연결
     * - JWT 토큰 필수 (URL 파라미터 ?token=... 또는 Authorization 헤더)
     */
    @GetMapping("/stream")
    @Operation(
            summary = "이미지 생성 진행률 SSE 스트림",
            description = "Server-Sent Events를 통해 이미지 생성 진행률을 실시간으로 받습니다. " +
                    "JWT 토큰이 필요합니다 (URL 파라미터 ?token=... 또는 Authorization 헤더)."
    )
    public SseEmitter streamGenerationProgress(
            @Parameter(hidden = true) Authentication authentication
    ) {
        // 로그인하지 않으면 에러
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        Long userId = AuthenticationUtil.getUserIdFromAuthentication(authentication);
        return sseEmitterService.createEmitter(userId);
    }

}
