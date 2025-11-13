package rheon.wsd_lora_community.training.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
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
import rheon.wsd_lora_community.training.dto.TrainingJobResponse;
import rheon.wsd_lora_community.training.entity.TrainingJob;
import rheon.wsd_lora_community.training.service.TrainingService;

import java.util.List;
import java.util.Map;

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
     * 학습 작업 시작 (FastAPI 연동)
     */
    @PostMapping("/jobs/{jobId}/start")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "학습 작업 시작", description = "대기 중인 학습 작업을 시작하고 FastAPI 서버로 학습 요청을 전송합니다.")
    public ResponseEntity<ApiResponse<Map<String, Object>>> startTraining(
            @Parameter(description = "학습 작업 ID", required = true)
            @PathVariable Long jobId,
            @RequestBody Map<String, Object> request
    ) {
        // 학습 작업 상태 업데이트
        Integer totalEpochs = (Integer) request.get("totalEpochs");
        TrainingJobResponse job = trainingService.startTraining(jobId, totalEpochs);

        // FastAPI로 학습 시작 요청 (비동기)
        String rawDatasetPath = (String) request.getOrDefault("rawDatasetPath", "./dataset");
        String outputDir = (String) request.getOrDefault("outputDir", "my_lora_model");
        Boolean skipPreprocessing = (Boolean) request.getOrDefault("skipPreprocessing", false);

        fastApiClient.startTraining(rawDatasetPath, outputDir, skipPreprocessing)
                .subscribe(
                        message -> {
                            // FastAPI 학습 시작 성공
                            System.out.println("FastAPI 학습 시작: " + message);
                        },
                        error -> {
                            // FastAPI 학습 시작 실패
                            System.err.println("FastAPI 학습 시작 실패: " + error.getMessage());
                            trainingService.failTraining(jobId, error.getMessage());
                        }
                );

        return ResponseEntity.ok(
                ApiResponse.success("학습 시작 성공. FastAPI 서버로 요청이 전송되었습니다.",
                        Map.of("job", job, "message", "학습이 백그라운드에서 시작되었습니다."))
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
            @PathVariable Long jobId,
            @RequestBody Map<String, String> request
    ) {
        String modelFileUrl = request.get("modelFileUrl");
        TrainingJobResponse job = trainingService.completeTraining(jobId, modelFileUrl);

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
}
