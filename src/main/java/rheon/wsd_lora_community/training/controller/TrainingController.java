package rheon.wsd_lora_community.training.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import rheon.wsd_lora_community.global.client.FastApiClient;
import rheon.wsd_lora_community.global.dto.ApiResponse;
import rheon.wsd_lora_community.global.service.S3UploadService;
import rheon.wsd_lora_community.global.websocket.GenerationProgressHandler;
import rheon.wsd_lora_community.training.dto.TrainingCallbackRequest;
import rheon.wsd_lora_community.training.dto.TrainingJobResponse;
import rheon.wsd_lora_community.training.entity.TrainingJob;
import rheon.wsd_lora_community.training.service.TrainingService;

import jakarta.validation.Valid;
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
    private final GenerationProgressHandler webSocketHandler;

    @Value("${app.callback-url}")
    private String callbackUrlBase;

    /**
     * 학습 작업 생성
     */
    @PostMapping("/models/{modelId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "학습 작업 생성", description = "모델의 학습 작업을 생성합니다. (모델 소유자만 가능)")
    public ResponseEntity<ApiResponse<TrainingJobResponse>> createTrainingJob(
            @Parameter(description = "모델 ID", required = true)
            @PathVariable Long modelId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal OAuth2User principal
    ) {
        Long userId = Long.valueOf(principal.getAttribute("id").toString());
        TrainingJobResponse job = trainingService.createTrainingJob(modelId, userId);

        return ResponseEntity.ok(
                ApiResponse.success("학습 작업 생성 성공", job)
        );
    }

    /**
     * 학습 이미지 업로드용 Presigned URL 생성
     */
    @PostMapping("/upload-urls")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "학습 이미지 업로드용 Presigned URL 생성",
            description = "학습 이미지를 S3에 업로드하기 위한 Presigned URL을 생성합니다. " +
                    "프론트엔드에서 이 URL로 이미지를 직접 업로드하고, 업로드된 이미지의 URL을 학습 시작 시 전달합니다. " +
                    "**요청 바디**: {\"jobId\": 123, \"fileNames\": [\"image1.png\", \"image2.png\"]}"
    )
    public ResponseEntity<ApiResponse<Map<String, Object>>> generateUploadUrls(
            @Parameter(hidden = true)
            @AuthenticationPrincipal OAuth2User principal,
            @RequestBody Map<String, Object> request
    ) {
        Long userId = Long.valueOf(principal.getAttribute("id").toString());

        // jobId 필수 파라미터
        Long jobId = request.get("jobId") != null
                ? Long.valueOf(request.get("jobId").toString())
                : null;

        if (jobId == null) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("학습 작업 ID가 필요합니다. (jobId)")
            );
        }

        @SuppressWarnings("unchecked")
        List<String> fileNames = (List<String>) request.get("fileNames");

        if (fileNames == null || fileNames.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("파일명 리스트가 필요합니다. (fileNames)")
            );
        }

        // 각 파일에 대한 업로드 URL과 S3 키 생성 (jobId 기반)
        List<Map<String, String>> uploadUrls = new ArrayList<>();
        List<String> downloadUrls = new ArrayList<>();

        for (String fileName : fileNames) {
            // 업로드용 Presigned URL 생성 (training-{jobId} 폴더)
            String uploadUrl = s3UploadService.generatePresignedUrl("training-" + jobId, fileName);

            // S3 키 생성 (다운로드용)
            String s3Key = s3UploadService.generateS3Key("training-" + jobId, fileName);

            // 다운로드용 Presigned URL 생성 (학습 시 Modal에 전달할 URL)
            String downloadUrl = s3UploadService.generateDownloadPresignedUrl(s3Key);

            Map<String, String> urlInfo = new HashMap<>();
            urlInfo.put("fileName", fileName);
            urlInfo.put("uploadUrl", uploadUrl);
            urlInfo.put("s3Key", s3Key);

            uploadUrls.add(urlInfo);
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
     */
    @PostMapping("/jobs/{jobId}/start")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "학습 작업 시작",
            description = """
                    대기 중인 학습 작업을 시작하고 Modal API로 학습 요청을 전송합니다.

                    **요청 바디 (JSON):**
                    - `totalEpochs` (Integer, 필수): 총 학습 에포크 수
                    - `modelName` (String, 필수): 모델 이름
                    - `trainingImageUrls` (List<String>, 필수): S3 Presigned URL 리스트
                    - `triggerWord` (String, 선택): 트리거 워드 (예: "sks", "ohwx")
                    - `epochs` (Integer, 선택): 학습 에포크 수 (Modal로 전달)
                    - `learningRate` (Double, 선택): 학습률 (예: 0.0001, 0.00002)
                    - `loraRank` (Integer, 선택): LoRA Rank (16, 32, 64)
                    - `baseModel` (String, 선택): 베이스 모델 (예: "stablediffusionapi/anything-v5")
                    - `skipPreprocessing` (Boolean, 선택): 전처리 스킵 여부 (기본값: false)

                    **예시:**
                    ```json
                    {
                      "totalEpochs": 10,
                      "modelName": "My Custom LoRA",
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
            @Parameter(description = "학습 작업 ID", required = true)
            @PathVariable Long jobId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "학습 시작 요청 파라미터",
                    required = true
            )
            @RequestBody Map<String, Object> request,
            @Parameter(hidden = true)
            @AuthenticationPrincipal OAuth2User principal
    ) {
        Long userId = Long.valueOf(principal.getAttribute("id").toString());

        // 학습 작업 상태 업데이트
        Integer totalEpochs = (Integer) request.get("totalEpochs");
        TrainingJobResponse job = trainingService.startTraining(jobId, totalEpochs);

        // 학습 이미지 S3 URL 리스트 (요청에서 받아옴)
        @SuppressWarnings("unchecked")
        List<String> trainingImageUrls = (List<String>) request.get("trainingImageUrls");

        if (trainingImageUrls == null || trainingImageUrls.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("학습 이미지 URL 리스트가 필요합니다. (trainingImageUrls)")
            );
        }

        // 모델 이름 가져오기
        String modelName = (String) request.get("modelName");
        if (modelName == null || modelName.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("모델 이름이 필요합니다. (modelName)")
            );
        }

        // 트리거 워드 (선택)
        String triggerWord = (String) request.get("triggerWord");

        // 학습 파라미터 (선택)
        Integer epochs = request.containsKey("epochs")
                ? (Integer) request.get("epochs")
                : null;
        Double learningRate = request.containsKey("learningRate")
                ? ((Number) request.get("learningRate")).doubleValue()
                : null;
        Integer loraRank = request.containsKey("loraRank")
                ? (Integer) request.get("loraRank")
                : null;
        String baseModel = (String) request.get("baseModel");

        // 전처리 스킵 여부 (선택, 기본값: false)
        Boolean skipPreprocessing = request.containsKey("skipPreprocessing")
                ? (Boolean) request.get("skipPreprocessing")
                : false;

        // Callback URL 설정 (Spring Boot 서버 URL)
        String baseUrl = request.containsKey("callbackBaseUrl")
                ? (String) request.get("callbackBaseUrl")
                : callbackUrlBase; // 기본값
        String callbackUrl = baseUrl + "/api/training/callback";

        // Modal API로 학습 시작 요청 (비동기)
        fastApiClient.startTraining(
                userId.toString(),
                job.getModelId(),
                jobId,
                modelName,
                trainingImageUrls,
                triggerWord,
                epochs,
                learningRate,
                loraRank,
                baseModel,
                skipPreprocessing,
                callbackUrl
        ).subscribe(
                message -> {
                    // Modal 학습 시작 성공
                    System.out.println("Modal 학습 시작: " + message);
                },
                error -> {
                    // Modal 학습 시작 실패
                    System.err.println("Modal 학습 시작 실패: " + error.getMessage());
                    trainingService.failTraining(jobId, error.getMessage());
                }
        );

        return ResponseEntity.ok(
                ApiResponse.success("학습 시작 성공. Modal GPU 서버로 요청이 전송되었습니다.",
                        Map.of("job", job, "message", "학습이 Modal에서 백그라운드로 시작되었습니다."))
        );
    }

    /**
     * 학습 진행률 업데이트
     */
    @PutMapping("/jobs/{jobId}/progress")
    @Operation(summary = "학습 진행률 업데이트", description = "학습 진행 상황을 업데이트합니다. (FastAPI 서버 전용)")
    public ResponseEntity<ApiResponse<TrainingJobResponse>> updateProgress(
            @Parameter(description = "학습 작업 ID", required = true)
            @PathVariable Long jobId,
            @RequestBody Map<String, Object> request
    ) {
        Integer currentEpoch = (Integer) request.get("currentEpoch");
        String phase = (String) request.get("phase");

        TrainingJobResponse job = trainingService.updateProgress(jobId, currentEpoch, phase);

        return ResponseEntity.ok(
                ApiResponse.success("진행률 업데이트 성공", job)
        );
    }

    /**
     * 학습 완료 처리
     */
    @PostMapping("/jobs/{jobId}/complete")
    @Operation(summary = "학습 완료 처리", description = "학습 작업을 완료 처리합니다. (FastAPI 서버 전용)")
    public ResponseEntity<ApiResponse<TrainingJobResponse>> completeTraining(
            @Parameter(description = "학습 작업 ID", required = true)
            @PathVariable Long jobId
    ) {
        TrainingJobResponse job = trainingService.completeTraining(jobId);

        return ResponseEntity.ok(
                ApiResponse.success("학습 완료 처리 성공", job)
        );
    }

    /**
     * 학습 실패 처리
     */
    @PostMapping("/jobs/{jobId}/fail")
    @Operation(summary = "학습 실패 처리", description = "학습 작업을 실패 처리합니다. (FastAPI 서버 전용)")
    public ResponseEntity<ApiResponse<TrainingJobResponse>> failTraining(
            @Parameter(description = "학습 작업 ID", required = true)
            @PathVariable Long jobId,
            @RequestBody Map<String, String> request
    ) {
        String errorMessage = request.get("errorMessage");
        TrainingJobResponse job = trainingService.failTraining(jobId, errorMessage);

        return ResponseEntity.ok(
                ApiResponse.success("학습 실패 처리 성공", job)
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
     * 모델의 최신 학습 작업 조회
     */
    @GetMapping("/models/{modelId}/latest")
    @Operation(summary = "모델의 최신 학습 작업 조회", description = "모델의 가장 최근 학습 작업을 조회합니다.")
    public ResponseEntity<ApiResponse<TrainingJobResponse>> getLatestTrainingJob(
            @Parameter(description = "모델 ID", required = true)
            @PathVariable Long modelId
    ) {
        TrainingJobResponse job = trainingService.getLatestTrainingJobByModel(modelId);

        return ResponseEntity.ok(
                ApiResponse.success("최신 학습 작업 조회 성공", job)
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
            @AuthenticationPrincipal OAuth2User principal
    ) {
        Long userId = Long.valueOf(principal.getAttribute("id").toString());
        List<TrainingJobResponse> jobs = trainingService.getUserTrainingJobs(userId);

        return ResponseEntity.ok(
                ApiResponse.success("학습 작업 목록 조회 성공", jobs)
        );
    }

    /**
     * 상태별 학습 작업 조회
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "상태별 학습 작업 조회 (관리자)", description = "특정 상태의 학습 작업을 조회합니다. (관리자만 가능)")
    public ResponseEntity<ApiResponse<List<TrainingJobResponse>>> getTrainingJobsByStatus(
            @Parameter(description = "학습 상태 (PENDING, IN_PROGRESS, COMPLETED, FAILED)", required = true)
            @PathVariable TrainingJob.TrainingStatus status
    ) {
        List<TrainingJobResponse> jobs = trainingService.getTrainingJobsByStatus(status);

        return ResponseEntity.ok(
                ApiResponse.success("학습 작업 조회 성공", jobs)
        );
    }

    /**
     * 진행 중인 학습 작업 조회
     */
    @GetMapping("/in-progress")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "진행 중인 학습 작업 조회 (관리자)", description = "현재 진행 중인 모든 학습 작업을 조회합니다. (관리자만 가능)")
    public ResponseEntity<ApiResponse<List<TrainingJobResponse>>> getInProgressJobs() {
        List<TrainingJobResponse> jobs = trainingService.getInProgressJobs();

        return ResponseEntity.ok(
                ApiResponse.success("진행 중인 학습 작업 조회 성공", jobs)
        );
    }

    // ========== SSE 스트리밍 ==========

    /**
     * 학습 진행률 실시간 스트리밍 (SSE)
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(
            summary = "학습 진행률 실시간 스트리밍 (SSE)",
            description = "FastAPI 서버의 학습 진행률을 Server-Sent Events로 실시간 스트리밍합니다. " +
                    "EventSource API를 사용하여 연결하세요."
    )
    public Flux<ServerSentEvent<Map<String, Object>>> streamTrainingProgress() {
        return fastApiClient.streamTrainingStatus()
                .map(status -> ServerSentEvent.<Map<String, Object>>builder()
                        .data(status)
                        .build())
                .doOnComplete(() -> System.out.println("학습 스트림 종료"))
                .doOnError(error -> System.err.println("학습 스트림 오류: " + error.getMessage()));
    }

    /**
     * FastAPI 서버 학습 상태 조회
     */
    @GetMapping("/fastapi/status")
    @Operation(summary = "FastAPI 학습 상태 조회", description = "FastAPI 서버의 현재 학습 상태를 조회합니다.")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getFastApiTrainingStatus() {
        return fastApiClient.getTrainingStatus()
                .map(status -> ResponseEntity.ok(
                        ApiResponse.success("FastAPI 학습 상태 조회 성공", status)
                ))
                .block(); // 블로킹 방식으로 변환 (동기 API)
    }

    // ========== FastAPI 콜백 엔드포인트 ==========

    /**
     * FastAPI 학습 진행률/완료/실패 콜백
     * - FastAPI 서버에서 학습 진행 중 또는 완료 시 호출됨
     * - 진행률을 DB에 저장하고 WebSocket으로 프론트엔드에 전달
     * - 인증 불필요 (FastAPI에서 직접 호출)
     */
    @PostMapping("/callback")
    @Operation(
            summary = "FastAPI 학습 콜백",
            description = "FastAPI 서버에서 학습 진행률 업데이트 또는 완료/실패 시 호출하는 콜백 엔드포인트입니다. " +
                    "진행 상태를 DB에 저장하고 WebSocket으로 사용자에게 알립니다."
    )
    public ResponseEntity<ApiResponse<?>> handleTrainingCallback(
            @RequestBody Map<String, Object> request
    ) {
        String status = (String) request.get("status");
        System.out.println("🔔 학습 콜백 수신: status=" + status + ", request=" + request);

        if ("LOADING".equals(status) || "PREPROCESSING".equals(status) || "TRAINING".equals(status) || "UPLOADING".equals(status)) {
            // 진행률 업데이트
            String userId = (String) request.get("userId");
            Integer modelId = request.get("modelId") != null
                    ? Integer.valueOf(request.get("modelId").toString())
                    : null;
            String message = (String) request.get("message");
            Long userIdLong = request.get("userId") != null
                    ? Long.valueOf(request.get("userId").toString())
                    : null;

            // DB 업데이트
            trainingService.handleTrainingProgress(userId, modelId, message);

            // WebSocket으로 진행률 전송
            if (userIdLong != null) {
                Map<String, Object> progressEvent = new HashMap<>();
                progressEvent.put("status", status);
                progressEvent.put("modelId", modelId);
                progressEvent.put("message", message);
                webSocketHandler.sendToUser(userIdLong, progressEvent);
            }

            return ResponseEntity.ok(
                    ApiResponse.success("진행률 업데이트 성공", Map.of(
                            "modelId", modelId != null ? modelId : 0,
                            "message", message != null ? message : ""
                    ))
            );
        } else if ("SUCCESS".equals(status)) {
            // 학습 성공
            String userId = (String) request.get("userId");
            Integer modelId = Integer.valueOf(request.get("modelId").toString());
            String modelName = (String) request.get("modelName");
            String s3ModelKey = (String) request.get("s3ModelKey");
            Long fileSize = request.get("fileSize") != null
                    ? Long.valueOf(request.get("fileSize").toString())
                    : null;
            Long userIdLong = Long.valueOf(userId);

            trainingService.handleTrainingCallback(userId, modelId, modelName, s3ModelKey, fileSize);

            // WebSocket으로 완료 이벤트 전송
            Map<String, Object> completionEvent = new HashMap<>();
            completionEvent.put("status", "SUCCESS");
            completionEvent.put("modelId", modelId);
            completionEvent.put("message", "Training completed successfully");
            completionEvent.put("s3ModelKey", s3ModelKey);

            webSocketHandler.sendToUser(userIdLong, completionEvent);

            return ResponseEntity.ok(
                    ApiResponse.success("학습 완료 콜백 처리 성공")
            );
        } else if ("FAIL".equals(status)) {
            // 학습 실패
            String userId = (String) request.get("userId");
            Integer modelId = request.get("modelId") != null
                    ? Integer.valueOf(request.get("modelId").toString())
                    : null;
            String error = (String) request.get("error");
            Long userIdLong = request.get("userId") != null
                    ? Long.valueOf(request.get("userId").toString())
                    : null;

            if (modelId != null) {
                trainingService.handleTrainingFailure(userId, modelId, error);

                // WebSocket으로 실패 이벤트 전송
                if (userIdLong != null) {
                    Map<String, Object> failureEvent = new HashMap<>();
                    failureEvent.put("status", "FAILED");
                    failureEvent.put("modelId", modelId);
                    failureEvent.put("message", error);

                    webSocketHandler.sendToUser(userIdLong, failureEvent);
                }
            }

            return ResponseEntity.ok(
                    ApiResponse.success("학습 실패 콜백 처리 완료", Map.of("error", error != null ? error : ""))
            );
        } else {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("잘못된 status 값: " + status));
        }
    }
}
