package rheon.wsd_lora_community.training.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import rheon.wsd_lora_community.global.client.FastApiClient;
import rheon.wsd_lora_community.global.queue.RedisQueueService;
import rheon.wsd_lora_community.global.queue.RedisQueueService.JobType;
import rheon.wsd_lora_community.global.service.JobCallbackService;
import rheon.wsd_lora_community.global.util.AuthenticationUtil;
import rheon.wsd_lora_community.global.dto.ApiResponse;
import rheon.wsd_lora_community.global.dto.ErrorResponse;
import rheon.wsd_lora_community.global.service.S3UploadService;
import rheon.wsd_lora_community.global.sse.SseEmitterService;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import rheon.wsd_lora_community.training.dto.TrainingCallbackRequest;
import rheon.wsd_lora_community.training.dto.TrainingJobResponse;
import rheon.wsd_lora_community.training.entity.TrainingJob;
import rheon.wsd_lora_community.training.service.TrainingService;

import jakarta.validation.Valid;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * 학습 작업 컨트롤러
 * - 학습 작업 생성/시작
 * - 학습 진행률 업데이트
 * - 학습 완료/실패 처리
 * - 학습 작업 조회
 */
@RestController
@RequestMapping("/api/training")
@RequiredArgsConstructor
@Tag(name = "Training", description = "학습 작업 API")
public class TrainingController {

    private final TrainingService trainingService;
    private final FastApiClient fastApiClient;
    private final S3UploadService s3UploadService;
    private final JobCallbackService jobCallbackService;
    private final RedisQueueService queueService;
    private final org.springframework.data.redis.core.RedisTemplate<String, Object> redisTemplate;
    private final SseEmitterService sseEmitterService;
    private final rheon.wsd_lora_community.global.queue.DynamicRedisPubSubService pubSubService;

    @Value("${app.callback-url}")
    private String callbackUrlBase;

    /**
     * 학습 이미지 업로드용 Presigned URL 생성
     * - userId 기반으로 업로드 URL 생성
     */
    @PostMapping("/upload-urls")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "학습 이미지 업로드용 Presigned URL 생성",
            description = "학습 이미지를 S3에 업로드하기 위한 Presigned URL을 생성합니다. " +
                    "프론트엔드에서 이 URL로 이미지를 직접 업로드하고, 업로드된 이미지의 URL을 학습 시작 시 전달합니다. " +
                    "**요청 바디**: {\"fileNames\": [\"image1.png\", \"image2.png\"]}"
    )
    public ResponseEntity<ApiResponse<Map<String, Object>>> generateUploadUrls(
            @Parameter(hidden = true)
            Authentication authentication,
            @RequestBody Map<String, Object> request
    ) {
        Long userId = AuthenticationUtil.getUserIdFromAuthentication(authentication);

        @SuppressWarnings("unchecked")
        List<String> fileNames = (List<String>) request.get("fileNames");

        if (fileNames == null || fileNames.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("파일명 리스트가 필요합니다. (fileNames)")
            );
        }

        // 각 파일에 대한 업로드 URL과 S3 키 생성 (userId + timestamp 기반)
        String folderName = "training-" + userId + "-" + System.currentTimeMillis();
        List<String> uploadUrls = new ArrayList<>();
        List<String> downloadUrls = new ArrayList<>();

        for (String fileName : fileNames) {
            // 업로드용 Presigned URL과 S3 키를 함께 생성
            Map<String, String> urlAndKey = s3UploadService.generatePresignedUrlWithKey(folderName, fileName);
            String uploadUrl = urlAndKey.get("uploadUrl");
            String s3Key = urlAndKey.get("s3Key");

            // 다운로드용 Presigned URL 생성 (학습 시 Modal에 전달할 URL)
            // training-data 버킷에서 다운로드하도록 명시적으로 버킷 이름 전달
            String downloadUrl = s3UploadService.generateTrainingDataDownloadUrl(s3Key);

            uploadUrls.add(uploadUrl);
            downloadUrls.add(downloadUrl);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("uploadUrls", uploadUrls);
        response.put("downloadUrls", downloadUrls);
        response.put("count", fileNames.size());

        return ResponseEntity.ok(
                ApiResponse.success("업로드 URL 생성 성공", response)
        );
    }

    /**
     * 학습 작업 시작 (Modal API 연동)
     * - TrainingJob 생성 및 학습 시작
     * - 모델은 학습 완료 후 생성됨
     */
    @PostMapping("/start")
    @PreAuthorize("isAuthenticated()")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "학습 작업 시작 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 (필수 파라미터 누락)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류 (FastAPI 연동 실패)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @Operation(
            summary = "학습 작업 시작",
            description = """
                    학습을 시작하고 Modal API로 학습 요청을 전송합니다.
                    학습 완료 후 모델이 자동으로 생성됩니다.

                    **요청 바디 (JSON):**
                    - `modelName` (String, 필수): 모델 이름
                    - `modelDescription` (String, 선택): 모델 설명
                    - `trainingImageUrls` (List<String>, 필수): S3 Presigned URL 리스트
                    - `triggerWord` (String, 선택): 트리거 워드 (예: "sks", "ohwx")
                    - `epochs` (Integer, 필수): 학습 에포크 수
                    - `learningRate` (Double, 선택): 학습률 (예: 0.0001, 0.00002)
                    - `loraRank` (Integer, 선택): LoRA Rank (16, 32, 64)
                    - `baseModel` (String, 선택): 베이스 모델 (기본값: "stablediffusionapi/anything-v5")
                    - `skipPreprocessing` (Boolean, 선택): 전처리 스킵 여부 (기본값: false)

                    **예시:**
                    ```json
                    {
                      "modelName": "My Custom LoRA",
                      "modelDescription": "A custom LoRA model",
                      "trainingImageUrls": ["https://s3.../image1.png", "https://s3.../image2.png"],
                      "triggerWord": "sks",
                      "epochs": 10,
                      "learningRate": 0.0001,
                      "loraRank": 32,
                      "baseModel": "stablediffusionapi/anything-v5",
                      "skipPreprocessing": false
                    }
                    ```
                    """
    )
    public ResponseEntity<ApiResponse<Map<String, Object>>> startTraining(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "학습 시작 요청 파라미터",
                    required = true
            )
            @RequestBody Map<String, Object> request,
            @Parameter(hidden = true)
            Authentication authentication
    ) {
        Long userId = AuthenticationUtil.getUserIdFromAuthentication(authentication);

        // 필수 파라미터 추출
        String modelName = (String) request.get("modelName");
        if (modelName == null || modelName.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("모델 이름이 필요합니다. (modelName)")
            );
        }

        @SuppressWarnings("unchecked")
        List<String> trainingImageUrls = (List<String>) request.get("trainingImageUrls");
        if (trainingImageUrls == null || trainingImageUrls.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("학습 이미지 URL 리스트가 필요합니다. (trainingImageUrls)")
            );
        }

        Integer epochs = request.get("epochs") != null
                ? ((Number) request.get("epochs")).intValue()
                : 10; // 기본값

        // 선택 파라미터
        String modelDescription = (String) request.get("modelDescription");
        String triggerWord = (String) request.get("triggerWord");
        Double learningRate = request.containsKey("learningRate")
                ? ((Number) request.get("learningRate")).doubleValue()
                : null;
        Integer loraRank = request.containsKey("loraRank")
                ? ((Number) request.get("loraRank")).intValue()
                : null;
        String baseModel = (String) request.getOrDefault("baseModel", "stablediffusionapi/anything-v5");
        Boolean skipPreprocessing = (Boolean) request.getOrDefault("skipPreprocessing", false);

        // TrainingJob 생성 및 학습 시작
        TrainingJobResponse job = trainingService.createAndStartTrainingJob(
                userId, modelName, modelDescription, trainingImageUrls.size(),
                epochs, learningRate, loraRank, baseModel, triggerWord
        );

        // Redis 큐에 작업 추가 (JobQueueWorker가 자동으로 처리)
        Map<String, Object> jobData = new HashMap<>();
        jobData.put("userId", userId);
        jobData.put("modelName", modelName);
        jobData.put("characterName", modelName);
        jobData.put("totalEpochs", epochs);
        jobData.put("trainingImageUrls", trainingImageUrls);
        jobData.put("triggerWord", triggerWord);
        jobData.put("learningRate", learningRate);
        jobData.put("loraRank", loraRank);
        jobData.put("baseModel", baseModel);
        jobData.put("skipPreprocessing", skipPreprocessing);
        jobData.put("datasetS3Path", ""); // JobQueueWorker에서 사용할 S3 경로 (필요시)

        queueService.enqueue(JobType.TRAINING, job.getId(), jobData);

        // ✅ Redis Pub/Sub 활성화 (실시간 진행률 전송)
        pubSubService.startTrainingJob();

        return ResponseEntity.ok(
                ApiResponse.success("학습 작업이 큐에 추가되었습니다. 순차적으로 처리됩니다.",
                        Map.of("job", job, "message", "학습이 대기열에 추가되었습니다."))
        );
    }

    /**
     * 학습 작업 상세 조회
     */
    @GetMapping("/jobs/{jobId}")
    @Operation(summary = "학습 작업 조회", description = "학습 작업의 상세 정보를 조회합니다.")
    public ResponseEntity<ApiResponse<TrainingJobResponse>> getTrainingJob(
            @Parameter(description = "학습 작업 ID", required = true)
            @PathVariable Long jobId
    ) {
        TrainingJobResponse job = trainingService.getTrainingJob(jobId);

        return ResponseEntity.ok(
                ApiResponse.success("학습 작업 조회 성공", job)
        );
    }

    /**
     * 내 학습 작업 목록 조회
     */
    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "내 학습 작업 목록 조회", description = "현재 유저의 모든 학습 작업을 조회합니다.")
    public ResponseEntity<ApiResponse<List<TrainingJobResponse>>> getMyTrainingJobs(
            @Parameter(hidden = true)
            Authentication authentication
    ) {
        Long userId = AuthenticationUtil.getUserIdFromAuthentication(authentication);
        List<TrainingJobResponse> jobs = trainingService.getUserTrainingJobs(userId);

        return ResponseEntity.ok(
                ApiResponse.success("학습 작업 목록 조회 성공", jobs)
        );
    }

    /**
     * 학습 작업 삭제
     * - 진행 중인 작업은 삭제할 수 없음
     * - 연결된 모델은 유지되며, 모델의 trainingJobId만 null로 설정됨
     */
    @DeleteMapping("/jobs/{jobId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "학습 작업 삭제", description = "완료되었거나 실패한 학습 작업을 삭제합니다. 진행 중인 작업은 삭제할 수 없습니다. 연결된 모델은 유지됩니다.")
    public ResponseEntity<ApiResponse<Void>> deleteTrainingJob(
            @Parameter(description = "학습 작업 ID", required = true)
            @PathVariable Long jobId,
            @Parameter(hidden = true)
            Authentication authentication
    ) {
        Long userId = AuthenticationUtil.getUserIdFromAuthentication(authentication);
        trainingService.deleteTrainingJob(jobId, userId);

        return ResponseEntity.ok(
                ApiResponse.success("학습 작업 삭제 성공", null)
        );
    }

    /**
     * 진행 중인 학습 작업 조회 (내 작업 중)
     */
    @GetMapping("/my/active")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "내 진행 중인 학습 작업 조회", description = "현재 유저의 진행 중인 학습 작업을 조회합니다. (PENDING, PREPROCESSING, TRAINING)")
    public ResponseEntity<ApiResponse<TrainingJobResponse>> getMyActiveTrainingJob(
            @Parameter(hidden = true)
            Authentication authentication
    ) {
        Long userId = AuthenticationUtil.getUserIdFromAuthentication(authentication);
        TrainingJobResponse activeJob = trainingService.getUserActiveTrainingJob(userId);

        if (activeJob == null) {
            return ResponseEntity.ok(
                    ApiResponse.success("진행 중인 학습 작업이 없습니다.", null)
            );
        }

        return ResponseEntity.ok(
                ApiResponse.success("진행 중인 학습 작업 조회 성공", activeJob)
        );
    }

    /**
     * 학습 진행률 조회 (Redis 캐시에서)
     * - 새로고침 시 진행률 복원용
     * - Redis에 TTL 1시간으로 저장된 진행 상황 반환
     */
    @GetMapping("/jobs/{jobId}/progress")
    @Operation(
            summary = "학습 진행률 조회 (캐시)",
            description = "Redis 캐시에 저장된 학습 진행 상황을 조회합니다. " +
                    "새로고침 후에도 진행률을 유지하기 위해 사용됩니다. " +
                    "TTL 1시간 동안 유지되며, 없으면 null 반환합니다."
    )
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTrainingProgress(
            @Parameter(description = "학습 작업 ID", required = true)
            @PathVariable Long jobId
    ) {
        String redisKey = "training:progress:" + jobId;
        Object cachedProgress = redisTemplate.opsForValue().get(redisKey);

        if (cachedProgress != null) {
            @SuppressWarnings("unchecked")
            Map<String, Object> progressData = (Map<String, Object>) cachedProgress;

            return ResponseEntity.ok(
                    ApiResponse.success("학습 진행률 조회 성공 (캐시)", progressData)
            );
        } else {
            return ResponseEntity.ok(
                    ApiResponse.success("캐시된 진행률 없음", null)
            );
        }
    }

    // ========== FastAPI 콜백 엔드포인트 ==========

    /**
     * FastAPI 학습 진행률/완료/실패 콜백
     * - FastAPI 서버에서 학습 진행 중 또는 완료 시 호출됨
     * - 진행률을 DB에 저장 (SSE는 Redis Pub/Sub에서 실시간 전송)
     * - 인증 불필요 (FastAPI에서 직접 호출)
     */
    @PostMapping("/callback")
    @Operation(
            summary = "FastAPI 학습 콜백",
            description = "FastAPI 서버에서 학습 진행률 업데이트 또는 완료/실패 시 호출하는 콜백 엔드포인트입니다. " +
                    "진행 상태를 DB에 저장합니다. 실시간 진행률은 Redis Pub/Sub을 통해 SSE로 전송됩니다."
    )
    public ResponseEntity<ApiResponse<?>> handleTrainingCallback(
            @Valid @RequestBody TrainingCallbackRequest request
    ) {
        String status = request.getStatus();
        System.out.println("🔔 학습 콜백 수신: status=" + status + ", jobId=" + request.getJobId() +
                           ", userId=" + request.getUserId() + ", message=" + request.getMessage());

        if ("LOADING".equals(status) || "DOWNLOADING".equals(status) || "DOWNLOADING_COMPLETE".equals(status) ||
            "PREPROCESSING".equals(status) || "CAPTIONING_COMPLETE".equals(status) ||
            "TRAINING".equals(status) || "UPLOADING".equals(status)) {
            // 진행률 업데이트
            Long jobId = request.getJobId();
            String message = request.getMessage();
            Integer currentEpoch = request.getCurrentEpoch();

            // message에서 epoch 정보 추출 시도 (예: "Training 24/70")
            if (message != null && message.contains("/") && "TRAINING".equals(status)) {
                try {
                    String[] parts = message.split(" ");
                    if (parts.length >= 2) {
                        String[] epochParts = parts[1].split("/");
                        if (epochParts.length == 2) {
                            currentEpoch = Integer.parseInt(epochParts[0]);
                        }
                    }
                } catch (Exception e) {
                    System.out.println("⚠️ Failed to parse epoch from message: " + message);
                }
            }

            jobCallbackService.handleTrainingProgress(jobId, currentEpoch, status, message);

            return ResponseEntity.ok(
                    ApiResponse.success("진행률 업데이트 성공", Map.of(
                            "jobId", jobId,
                            "message", message != null ? message : ""
                    ))
            );
        } else if ("SUCCESS".equals(status)) {
            // 학습 성공 - 모델 생성
            Long jobId = request.getJobId();
            Long userId = request.getUserId();
            String s3ModelKey = request.getS3ModelKey();
            Long fileSize = request.getFileSize();

            try {
                Long modelId = jobCallbackService.handleTrainingSuccess(jobId, userId, s3ModelKey, fileSize);
                return ResponseEntity.ok(
                        ApiResponse.success("학습 완료 콜백 처리 성공", Map.of("modelId", modelId))
                );
            } catch (Exception e) {
                System.err.println("❌ 학습 완료 처리 실패: " + e.getMessage());
                e.printStackTrace();
                return ResponseEntity.status(500).body(
                        ApiResponse.error("학습 완료 처리 실패: " + e.getMessage())
                );
            }
        } else if ("FAIL".equals(status)) {
            // 학습 실패
            Long jobId = request.getJobId();
            Long userId = request.getUserId();
            String error = request.getError();

            jobCallbackService.handleTrainingFailure(jobId, userId, error);

            return ResponseEntity.ok(
                    ApiResponse.success("학습 실패 콜백 처리 완료", Map.of("error", error != null ? error : ""))
            );
        } else {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("잘못된 status 값: " + status));
        }
    }

    // ========== SSE (Server-Sent Events) 엔드포인트 ==========

    /**
     * SSE 연결 (학습 진행률 실시간 스트리밍)
     * - Redis Pub/Sub 메시지를 SSE로 전송
     * - 새로고침 시 자동 재연결
     */
    @GetMapping("/stream")
    @Operation(
            summary = "학습 진행률 SSE 스트림",
            description = "Server-Sent Events를 통해 학습 진행률을 실시간으로 받습니다. " +
                    "Redis Pub/Sub 메시지가 도착하면 자동으로 전송됩니다."
    )
    public SseEmitter streamTrainingProgress(
            @Parameter(hidden = true) Authentication authentication
    ) {
        Long userId = AuthenticationUtil.getUserIdFromAuthentication(authentication);
        return sseEmitterService.createEmitter(userId);
    }
}
