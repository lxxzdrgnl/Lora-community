package rheon.wsd_lora_community.global.queue;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import rheon.wsd_lora_community.generation.service.GenerationService;
import rheon.wsd_lora_community.global.client.FastApiClient;
import rheon.wsd_lora_community.global.queue.RedisQueueService.JobType;
import rheon.wsd_lora_community.training.service.TrainingService;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Redis 작업 큐 워커
 * - 백그라운드 스레드로 실행
 * - Redis 큐에서 작업을 꺼내서 FastAPI로 요청 전송
 * - Training과 Generation 각각 별도 워커 스레드 실행
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JobQueueWorker {

    private final RedisQueueService queueService;
    private final FastApiClient fastApiClient;
    private final TrainingService trainingService;
    private final GenerationService generationService;

    @Value("${app.callback-url}")
    private String callbackUrlBase;

    private final ExecutorService trainingWorker = Executors.newSingleThreadExecutor();
    private final ExecutorService generationWorker = Executors.newSingleThreadExecutor();

    private volatile boolean running = false;

    /**
     * 서버 시작 시 워커 스레드 시작
     */
    @PostConstruct
    public void startWorkers() {
        running = true;
        log.info("🚀 작업 큐 워커 시작");

        // Training 워커 시작
        trainingWorker.submit(this::processTrainingQueue);

        // Generation 워커 시작
        generationWorker.submit(this::processGenerationQueue);
    }

    /**
     * 서버 종료 시 워커 스레드 정리
     */
    @PreDestroy
    public void stopWorkers() {
        running = false;
        log.info("🛑 작업 큐 워커 중지 중...");

        trainingWorker.shutdown();
        generationWorker.shutdown();

        try {
            if (!trainingWorker.awaitTermination(10, TimeUnit.SECONDS)) {
                trainingWorker.shutdownNow();
            }
            if (!generationWorker.awaitTermination(10, TimeUnit.SECONDS)) {
                generationWorker.shutdownNow();
            }
            log.info("✅ 작업 큐 워커 중지 완료");
        } catch (InterruptedException e) {
            trainingWorker.shutdownNow();
            generationWorker.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Training 큐 처리 워커
     */
    private void processTrainingQueue() {
        log.info("🔄 Training 큐 워커 시작");

        while (running) {
            try {
                // Redis 큐에서 작업 꺼내기 (블로킹, 최대 5초 대기)
                Long jobId = queueService.dequeue(JobType.TRAINING);

                if (jobId == null) {
                    continue; // 큐가 비어있으면 다시 대기
                }

                log.info("📤 Training 작업 처리 시작: jobId={}", jobId);

                // 작업 정보 조회
                Map<String, String> jobDetail = queueService.getJobDetail(JobType.TRAINING, jobId);
                String userId = jobDetail.get("userId");
                String modelName = jobDetail.get("modelName");
                Integer totalEpochs = Integer.parseInt(jobDetail.get("totalEpochs"));
                String triggerWord = jobDetail.get("triggerWord");
                Double learningRate = jobDetail.get("learningRate") != null && !jobDetail.get("learningRate").isEmpty()
                        ? Double.parseDouble(jobDetail.get("learningRate"))
                        : null;
                Integer loraRank = jobDetail.get("loraRank") != null && !jobDetail.get("loraRank").isEmpty()
                        ? Integer.parseInt(jobDetail.get("loraRank"))
                        : null;
                String baseModel = jobDetail.get("baseModel");
                Boolean skipPreprocessing = Boolean.parseBoolean(jobDetail.get("skipPreprocessing"));

                // trainingImageUrls는 JSON으로 저장되어 있으므로 파싱 필요
                // 간단하게 처리하기 위해 null로 전달 (FastAPI가 S3에서 직접 가져올 수 있다고 가정)
                java.util.List<String> trainingImageUrls = null;

                // FastAPI로 학습 요청 전송
                String callbackUrl = callbackUrlBase + "/api/training/callback";

                fastApiClient.startTraining(
                    userId,
                    null,  // datasetS3Path
                    jobId,
                    modelName,
                    trainingImageUrls,  // null
                    triggerWord,
                    totalEpochs,
                    learningRate,
                    loraRank,
                    baseModel,
                    skipPreprocessing,
                    callbackUrl
                ).subscribe(
                    response -> log.info("✅ FastAPI 학습 요청 전송 성공: jobId={}", jobId),
                    error -> {
                        log.error("❌ FastAPI 학습 요청 실패: jobId={}, error={}", jobId, error.getMessage());
                        // 실패 처리
                        trainingService.failTraining(jobId, "FastAPI 요청 실패: " + error.getMessage());
                        queueService.fail(JobType.TRAINING, jobId);
                    }
                );

            } catch (Exception e) {
                log.error("❌ Training 큐 처리 중 오류: {}", e.getMessage());
                // Redis 연결 오류 시 긴 대기 시간 적용
                try {
                    if (e.getMessage() != null && e.getMessage().contains("Unable to connect to Redis")) {
                        log.warn("⚠️ Redis 연결 실패, 30초 후 재시도...");
                        Thread.sleep(30000); // 30초 대기
                    } else {
                        Thread.sleep(5000); // 일반 오류는 5초 대기
                    }
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        log.info("🛑 Training 큐 워커 종료");
    }

    /**
     * Generation 큐 처리 워커
     */
    private void processGenerationQueue() {
        log.info("🔄 Generation 큐 워커 시작");

        while (running) {
            try {
                // Redis 큐에서 작업 꺼내기 (블로킹, 최대 5초 대기)
                Long historyId = queueService.dequeue(JobType.GENERATION);

                if (historyId == null) {
                    continue; // 큐가 비어있으면 다시 대기
                }

                log.info("📤 Generation 작업 처리 시작: historyId={}", historyId);

                // 작업 정보 조회
                Map<String, String> jobDetail = queueService.getJobDetail(JobType.GENERATION, historyId);
                Long userId = Long.parseLong(jobDetail.get("userId"));
                Long modelId = Long.parseLong(jobDetail.get("modelId"));
                String modelPath = jobDetail.get("modelPath");
                String prompt = jobDetail.get("prompt");
                String negativePrompt = jobDetail.get("negativePrompt");
                Integer steps = Integer.parseInt(jobDetail.get("steps"));
                Double guidanceScale = Double.parseDouble(jobDetail.get("guidanceScale"));
                Double loraScale = Double.parseDouble(jobDetail.get("loraScale"));
                Long seed = jobDetail.get("seed") != null ? Long.parseLong(jobDetail.get("seed")) : null;
                Integer numImages = Integer.parseInt(jobDetail.get("numImages"));

                // FastAPI로 이미지 생성 요청 전송
                String callbackUrl = callbackUrlBase + "/api/generate/callback";

                fastApiClient.startImageGeneration(
                    userId.toString(),      // String userId
                    modelId,                // Long modelId
                    historyId,              // Long historyId
                    prompt,                 // String prompt
                    negativePrompt,         // String negativePrompt
                    modelPath,              // String loraModelUrl
                    numImages,              // int numImages
                    steps,                  // int steps
                    guidanceScale,          // double guidanceScale
                    loraScale,              // Double loraScale
                    seed,                   // Long seed
                    null,                   // String baseModel (선택사항)
                    callbackUrl             // String callbackUrl
                ).subscribe(
                    response -> log.info("✅ FastAPI 생성 요청 전송 성공: historyId={}", historyId),
                    error -> {
                        log.error("❌ FastAPI 생성 요청 실패: historyId={}, error={}", historyId, error.getMessage());
                        // 실패 처리
                        generationService.failGeneration(historyId, "FastAPI 요청 실패: " + error.getMessage());
                        queueService.fail(JobType.GENERATION, historyId);
                    }
                );

            } catch (Exception e) {
                log.error("❌ Generation 큐 처리 중 오류: {}", e.getMessage());
                // Redis 연결 오류 시 긴 대기 시간 적용
                try {
                    if (e.getMessage() != null && e.getMessage().contains("Unable to connect to Redis")) {
                        log.warn("⚠️ Redis 연결 실패, 30초 후 재시도...");
                        Thread.sleep(30000); // 30초 대기
                    } else {
                        Thread.sleep(5000); // 일반 오류는 5초 대기
                    }
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        log.info("🛑 Generation 큐 워커 종료");
    }
}
