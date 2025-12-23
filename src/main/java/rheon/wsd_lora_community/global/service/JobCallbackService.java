package rheon.wsd_lora_community.global.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rheon.wsd_lora_community.generation.dto.GenerationHistoryResponse;
import rheon.wsd_lora_community.generation.entity.GenerationHistory;
import rheon.wsd_lora_community.generation.repository.GenerationHistoryRepository;
import rheon.wsd_lora_community.generation.service.GenerationService;
import rheon.wsd_lora_community.global.exception.CustomException;
import rheon.wsd_lora_community.global.exception.ErrorCode;
import rheon.wsd_lora_community.global.queue.RedisQueueService;
import rheon.wsd_lora_community.global.queue.RedisQueueService.JobType;
import rheon.wsd_lora_community.global.sse.SseEmitterService;
import rheon.wsd_lora_community.training.dto.TrainingJobResponse;
import rheon.wsd_lora_community.training.entity.TrainingJob;
import rheon.wsd_lora_community.training.repository.TrainingJobRepository;
import rheon.wsd_lora_community.training.service.TrainingService;

import java.util.HashMap;
import java.util.Map;

/**
 * 학습/생성 작업 콜백 처리 공통 서비스
 * - Training과 Generation의 콜백 로직을 통합
 * - Redis 작업 큐 관리
 * - SSE를 통한 실시간 진행률 전송
 */
@Slf4j
@Service
public class JobCallbackService {

    private final TrainingService trainingService;
    private final GenerationService generationService;
    private final RedisQueueService queueService;
    private final SseEmitterService sseEmitterService;
    private final GenerationHistoryRepository generationHistoryRepository;
    private final TrainingJobRepository trainingJobRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final rheon.wsd_lora_community.global.queue.DynamicRedisPubSubService pubSubService;

    // 순환 참조 방지: DynamicRedisPubSubService를 @Lazy로 주입
    public JobCallbackService(
            TrainingService trainingService,
            GenerationService generationService,
            RedisQueueService queueService,
            SseEmitterService sseEmitterService,
            GenerationHistoryRepository generationHistoryRepository,
            TrainingJobRepository trainingJobRepository,
            RedisTemplate<String, Object> redisTemplate,
            @Lazy rheon.wsd_lora_community.global.queue.DynamicRedisPubSubService pubSubService
    ) {
        this.trainingService = trainingService;
        this.generationService = generationService;
        this.queueService = queueService;
        this.sseEmitterService = sseEmitterService;
        this.generationHistoryRepository = generationHistoryRepository;
        this.trainingJobRepository = trainingJobRepository;
        this.redisTemplate = redisTemplate;
        this.pubSubService = pubSubService;
    }

    /**
     * 학습 진행률 콜백 처리
     */
    @Transactional
    public void handleTrainingProgress(Long jobId, Integer currentEpoch, String phase, String message) {
        if (jobId == null) {
            log.warn("⚠️ jobId가 없습니다. 진행률 업데이트 불가.");
            return;
        }

        // DB 업데이트
        trainingService.updateProgress(jobId, currentEpoch, phase);

        // SSE로 실시간 진행률 전송
        try {
            TrainingJob trainingJob = trainingJobRepository.findById(jobId)
                    .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));

            Long userId = trainingJob.getUser().getId();

            Map<String, Object> progressData = new HashMap<>();
            progressData.put("status", "TRAINING");
            progressData.put("jobId", jobId);
            progressData.put("currentEpoch", currentEpoch != null ? currentEpoch : 0);
            progressData.put("phase", phase != null ? phase : "");
            progressData.put("message", message != null ? message : "Training...");

            sseEmitterService.sendToUser(userId, "training_progress", progressData);

            log.debug("✅ Training progress SSE sent: jobId={}, epoch={}, phase={}", jobId, currentEpoch, phase);
        } catch (Exception e) {
            log.error("❌ SSE 전송 실패 (학습 진행률): jobId={}", jobId, e);
            // SSE 전송 실패해도 DB는 업데이트되었으므로 무시
        }
    }

    /**
     * 학습 성공 콜백 처리
     */
    @Transactional
    public Long handleTrainingSuccess(Long jobId, Long userId, String s3ModelKey, Long fileSize) {
        // jobId가 없으면 userId로 진행 중인 작업 찾기
        if (jobId == null && userId != null) {
            log.warn("⚠️ jobId가 없어서 userId로 진행 중인 학습 작업을 찾습니다. userId: {}", userId);
            TrainingJobResponse activeJob = trainingService.getUserActiveTrainingJob(userId);
            if (activeJob != null) {
                jobId = activeJob.getId();
                log.info("✅ 진행 중인 학습 작업 발견: jobId={}", jobId);
            } else {
                log.error("❌ 진행 중인 학습 작업을 찾을 수 없습니다. userId: {}", userId);
                throw new IllegalArgumentException("진행 중인 학습 작업을 찾을 수 없습니다. (userId=" + userId + ")");
            }
        } else if (jobId == null) {
            log.error("❌ jobId와 userId 모두 없습니다.");
            throw new IllegalArgumentException("학습 작업 ID 또는 사용자 ID가 필요합니다. (jobId or userId)");
        }

        // ✅ 중복 방지: 이미 완료된 작업인지 확인
        TrainingJob existingJob = trainingJobRepository.findById(jobId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));

        if ("COMPLETED".equals(existingJob.getStatus().name())) {
            log.info("⏭️ [중복 방지] 이미 완료된 작업: jobId={}, 스킵합니다.", jobId);
            return existingJob.getModel() != null ? existingJob.getModel().getId() : null;
        }

        // 모델 생성 및 TrainingJob 완료 처리
        Long modelId = trainingService.handleTrainingSuccess(jobId, s3ModelKey, fileSize);
        log.info("✅ 학습 완료 처리 성공: jobId={}, modelId={}", jobId, modelId);

        // SSE로 완료 알림 전송
        try {
            TrainingJob trainingJob = trainingJobRepository.findById(jobId)
                    .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));

            Map<String, Object> successData = new HashMap<>();
            successData.put("status", "COMPLETED");
            successData.put("jobId", jobId);
            successData.put("modelId", modelId);
            successData.put("message", "Training completed successfully!");

            sseEmitterService.sendToUser(trainingJob.getUser().getId(), "training_progress", successData);

            log.info("✅ Training SUCCESS SSE sent: jobId={}, modelId={}, userId={}", jobId, modelId, trainingJob.getUser().getId());
        } catch (Exception e) {
            log.error("❌ SSE 전송 실패 (학습 완료): jobId={}", jobId, e);
            // SSE 전송 실패해도 DB는 업데이트되었으므로 무시
        }

        // Redis 작업 완료 처리
        queueService.complete(JobType.TRAINING, jobId);

        // Redis 캐시 삭제 (TTL 만료 전에 수동 삭제)
        try {
            String cacheKey = "training:progress:" + jobId;
            Boolean deleted = redisTemplate.delete(cacheKey);
            if (Boolean.TRUE.equals(deleted)) {
                log.info("🗑️ Redis 캐시 삭제 완료: {}", cacheKey);
            }
        } catch (Exception e) {
            log.warn("⚠️ Redis 캐시 삭제 실패 (무시): jobId={}", jobId, e);
        }

        // Redis Pub/Sub 비활성화 (비용 절감)
        pubSubService.stopTrainingJob();

        return modelId;
    }

    /**
     * 학습 실패 콜백 처리
     */
    @Transactional
    public void handleTrainingFailure(Long jobId, Long userId, String error) {
        // jobId가 없으면 userId로 진행 중인 작업 찾기
        if (jobId == null && userId != null) {
            log.warn("⚠️ jobId가 없어서 userId로 진행 중인 학습 작업을 찾습니다. userId: {}", userId);
            TrainingJobResponse activeJob = trainingService.getUserActiveTrainingJob(userId);
            if (activeJob != null) {
                jobId = activeJob.getId();
                log.info("✅ 진행 중인 학습 작업 발견: jobId={}", jobId);
            } else {
                log.error("❌ 진행 중인 학습 작업을 찾을 수 없습니다. userId: {}", userId);
                return; // 실패 처리는 조용히 무시
            }
        }

        if (jobId != null) {
            trainingService.failTraining(jobId, error);
            log.info("✅ 학습 실패 처리 완료: jobId={}, error={}", jobId, error);

            // SSE로 실패 알림 전송
            try {
                TrainingJob trainingJob = trainingJobRepository.findById(jobId)
                        .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));

                Map<String, Object> failData = new HashMap<>();
                failData.put("status", "FAILED");
                failData.put("jobId", jobId);
                failData.put("message", error != null ? error : "Training failed");

                sseEmitterService.sendToUser(trainingJob.getUser().getId(), "training_progress", failData);

                log.info("✅ Training FAILED SSE sent: jobId={}, userId={}", jobId, trainingJob.getUser().getId());
            } catch (Exception e) {
                log.error("❌ SSE 전송 실패 (학습 실패): jobId={}", jobId, e);
                // SSE 전송 실패해도 DB는 업데이트되었으므로 무시
            }

            // Redis 작업 실패 처리
            queueService.fail(JobType.TRAINING, jobId);

            // Redis 캐시 삭제 (TTL 만료 전에 수동 삭제)
            try {
                String cacheKey = "training:progress:" + jobId;
                Boolean deleted = redisTemplate.delete(cacheKey);
                if (Boolean.TRUE.equals(deleted)) {
                    log.info("🗑️ Redis 캐시 삭제 완료 (실패): {}", cacheKey);
                }
            } catch (Exception e) {
                log.warn("⚠️ Redis 캐시 삭제 실패 (무시): jobId={}", jobId, e);
            }

            // Redis Pub/Sub 비활성화 (비용 절감)
            pubSubService.stopTrainingJob();
        }
    }

    /**
     * 생성 진행률 콜백 처리
     */
    @Transactional
    public void handleGenerationProgress(Long historyId, Integer currentStep, Integer totalSteps, String message) {
        if (historyId == null) {
            log.warn("⚠️ historyId가 없습니다. 진행률 업데이트 불가.");
            return;
        }

        // DB 업데이트
        generationService.updateProgress(historyId, currentStep, totalSteps);

        // SSE로 실시간 진행률 전송
        try {
            GenerationHistory history = generationHistoryRepository.findById(historyId)
                    .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));

            Long userId = history.getUser().getId();

            Map<String, Object> progressData = new HashMap<>();
            progressData.put("status", "GENERATING");
            progressData.put("historyId", historyId);
            progressData.put("currentStep", currentStep != null ? currentStep : 0);
            progressData.put("totalSteps", totalSteps != null ? totalSteps : 0);
            progressData.put("message", message != null ? message : "Generating...");

            sseEmitterService.sendToUser(userId, "generation_progress", progressData);

            log.debug("✅ Generation progress SSE sent: historyId={}, step={}/{}", historyId, currentStep, totalSteps);
        } catch (Exception e) {
            log.error("❌ SSE 전송 실패 (진행률): historyId={}", historyId, e);
            // SSE 전송 실패해도 DB는 업데이트되었으므로 무시
        }
    }

    /**
     * 생성 성공 콜백 처리
     */
    @Transactional
    public GenerationHistoryResponse handleGenerationSuccess(
            Long historyId,
            Long userId,
            Long modelId,
            String prompt,
            String negativePrompt,
            Integer steps,
            Double guidanceScale,
            Double loraScale,
            Long seed,
            Integer numImages,
            java.util.List<String> imageS3Keys
    ) {
        // historyId가 없으면 userId로 진행 중인 작업 찾기
        if (historyId == null && userId != null) {
            log.warn("⚠️ historyId가 없어서 userId로 진행 중인 생성 작업을 찾습니다. userId: {}", userId);
            GenerationHistoryResponse ongoingGeneration = generationService.getOngoingGeneration(userId);
            if (ongoingGeneration != null) {
                historyId = ongoingGeneration.getId();
                log.info("✅ 진행 중인 생성 작업 발견: historyId={}", historyId);
            } else {
                log.error("❌ 진행 중인 생성 작업을 찾을 수 없습니다. userId: {}", userId);
                throw new IllegalArgumentException("진행 중인 생성 작업을 찾을 수 없습니다. (userId=" + userId + ")");
            }
        } else if (historyId == null) {
            log.error("❌ historyId와 userId 모두 없습니다.");
            throw new IllegalArgumentException("생성 기록 ID 또는 사용자 ID가 필요합니다. (historyId or userId)");
        }

        // ✅ 중복 방지: 이미 완료된 작업인지 확인
        GenerationHistory existingHistory = generationHistoryRepository.findById(historyId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));

        if ("SUCCESS".equals(existingHistory.getStatus())) {
            log.info("⏭️ [중복 방지] 이미 완료된 작업: historyId={}, 스킵합니다.", historyId);
            return generationService.getGenerationHistory(historyId);
        }

        GenerationHistoryResponse history = generationService.completeGeneration(
                historyId, modelId, userId, prompt, negativePrompt, steps, guidanceScale, loraScale, seed, numImages, imageS3Keys
        );
        log.info("✅ 생성 완료 처리 성공: historyId={}, images={}", historyId, imageS3Keys != null ? imageS3Keys.size() : 0);

        // SSE로 완료 알림 전송 (프론트엔드가 이미지 표시)
        try {
            Map<String, Object> successData = new HashMap<>();
            successData.put("status", "SUCCESS");
            successData.put("historyId", historyId);
            successData.put("message", "Generation completed!");
            successData.put("generatedImages", history.getGeneratedImages()); // 프론트엔드에서 이미지 URL 추출

            sseEmitterService.sendToUser(history.getUserId(), "generation_progress", successData);

            log.info("✅ Generation SUCCESS SSE sent: historyId={}, userId={}", historyId, history.getUserId());
        } catch (Exception e) {
            log.error("❌ SSE 전송 실패 (완료): historyId={}", historyId, e);
            // SSE 전송 실패해도 DB는 업데이트되었으므로 무시
        }

        // Redis 작업 완료 처리
        queueService.complete(JobType.GENERATION, historyId);

        // Redis 캐시 삭제 (TTL 만료 전에 수동 삭제)
        try {
            String progressKey = "generation:progress:" + historyId;
            String completionKey = "generation:completion:" + historyId;

            Long deletedCount = redisTemplate.delete(java.util.Arrays.asList(progressKey, completionKey));
            if (deletedCount != null && deletedCount > 0) {
                log.info("🗑️ Redis 캐시 삭제 완료: {} 개 키 삭제", deletedCount);
            }
        } catch (Exception e) {
            log.warn("⚠️ Redis 캐시 삭제 실패 (무시): historyId={}", historyId, e);
        }

        // Redis Pub/Sub 비활성화 (비용 절감, 모든 작업 완료 시 자동 중지)
        pubSubService.stopGenerationJob();

        return history;
    }

    /**
     * 생성 실패 콜백 처리
     */
    @Transactional
    public void handleGenerationFailure(Long historyId, Long userId, String error) {
        // historyId가 없으면 userId로 진행 중인 작업 찾기
        if (historyId == null && userId != null) {
            log.warn("⚠️ historyId가 없어서 userId로 진행 중인 생성 작업을 찾습니다. userId: {}", userId);
            GenerationHistoryResponse ongoingGeneration = generationService.getOngoingGeneration(userId);
            if (ongoingGeneration != null) {
                historyId = ongoingGeneration.getId();
                log.info("✅ 진행 중인 생성 작업 발견: historyId={}", historyId);
            } else {
                log.error("❌ 진행 중인 생성 작업을 찾을 수 없습니다. userId: {}", userId);
                return; // 실패 처리는 조용히 무시
            }
        }

        if (historyId != null) {
            generationService.failGeneration(historyId, error);
            log.info("✅ 생성 실패 처리 완료: historyId={}, error={}", historyId, error);

            // SSE로 실패 알림 전송
            try {
                GenerationHistory history = generationHistoryRepository.findById(historyId)
                        .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));

                Map<String, Object> failData = new HashMap<>();
                failData.put("status", "FAILED");
                failData.put("historyId", historyId);
                failData.put("message", error != null ? error : "Generation failed");

                sseEmitterService.sendToUser(history.getUser().getId(), "generation_progress", failData);

                log.info("✅ Generation FAILED SSE sent: historyId={}, userId={}", historyId, history.getUser().getId());
            } catch (Exception e) {
                log.error("❌ SSE 전송 실패 (실패): historyId={}", historyId, e);
                // SSE 전송 실패해도 DB는 업데이트되었으므로 무시
            }

            // Redis 작업 실패 처리
            queueService.fail(JobType.GENERATION, historyId);

            // Redis 캐시 삭제 (TTL 만료 전에 수동 삭제)
            try {
                String progressKey = "generation:progress:" + historyId;
                String completionKey = "generation:completion:" + historyId;

                Long deletedCount = redisTemplate.delete(java.util.Arrays.asList(progressKey, completionKey));
                if (deletedCount != null && deletedCount > 0) {
                    log.info("🗑️ Redis 캐시 삭제 완료 (실패): {} 개 키 삭제", deletedCount);
                }
            } catch (Exception e) {
                log.warn("⚠️ Redis 캐시 삭제 실패 (무시): historyId={}", historyId, e);
            }

            // Redis Pub/Sub 비활성화 (비용 절감, 모든 작업 완료 시 자동 중지)
            pubSubService.stopGenerationJob();
        }
    }
}
