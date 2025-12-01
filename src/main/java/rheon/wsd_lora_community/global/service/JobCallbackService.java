package rheon.wsd_lora_community.global.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rheon.wsd_lora_community.generation.dto.GenerationHistoryResponse;
import rheon.wsd_lora_community.generation.service.GenerationService;
import rheon.wsd_lora_community.global.websocket.GenerationProgressHandler;
import rheon.wsd_lora_community.training.dto.TrainingJobResponse;
import rheon.wsd_lora_community.training.entity.TrainingJob;
import rheon.wsd_lora_community.training.service.TrainingService;

import java.util.HashMap;
import java.util.Map;

/**
 * 학습/생성 작업 콜백 처리 공통 서비스
 * - Training과 Generation의 콜백 로직을 통합
 * - GPU 리소스 관리 및 WebSocket 전송 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JobCallbackService {

    private final TrainingService trainingService;
    private final GenerationService generationService;
    private final GpuResourceManager gpuResourceManager;
    private final GenerationProgressHandler webSocketHandler;

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

        // WebSocket으로 진행률 전송
        TrainingJob job = trainingService.getTrainingJobEntity(jobId);
        Map<String, Object> progressEvent = new HashMap<>();
        progressEvent.put("status", phase);
        progressEvent.put("jobId", jobId);
        progressEvent.put("message", message);
        progressEvent.put("currentEpoch", currentEpoch);
        webSocketHandler.sendToUser(job.getUser().getId(), progressEvent);

        log.debug("✅ Training progress updated: jobId={}, epoch={}, phase={}", jobId, currentEpoch, phase);
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

        // 모델 생성 및 TrainingJob 완료 처리
        Long modelId = trainingService.handleTrainingSuccess(jobId, s3ModelKey, fileSize);
        log.info("✅ 학습 완료 처리 성공: jobId={}, modelId={}", jobId, modelId);

        // GPU 리소스 반환
        gpuResourceManager.release(jobId);

        // WebSocket으로 완료 이벤트 전송
        Map<String, Object> completionEvent = new HashMap<>();
        completionEvent.put("status", "SUCCESS");
        completionEvent.put("modelId", modelId);
        completionEvent.put("message", "Training completed successfully");
        completionEvent.put("s3ModelKey", s3ModelKey);

        TrainingJob job = trainingService.getTrainingJobEntity(jobId);
        webSocketHandler.sendToUser(job.getUser().getId(), completionEvent);

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

            // GPU 리소스 반환
            gpuResourceManager.release(jobId);

            // WebSocket으로 실패 이벤트 전송
            TrainingJob job = trainingService.getTrainingJobEntity(jobId);
            Map<String, Object> failureEvent = new HashMap<>();
            failureEvent.put("status", "FAILED");
            failureEvent.put("jobId", jobId);
            failureEvent.put("message", error);

            webSocketHandler.sendToUser(job.getUser().getId(), failureEvent);
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

        // WebSocket으로 진행률 전송
        GenerationHistoryResponse history = generationService.getGenerationHistory(historyId);
        Map<String, Object> progressEvent = new HashMap<>();
        progressEvent.put("status", "GENERATING");
        progressEvent.put("historyId", historyId);
        progressEvent.put("message", message);
        progressEvent.put("currentStep", currentStep);
        progressEvent.put("totalSteps", totalSteps);
        webSocketHandler.sendToUser(history.getUserId(), progressEvent);

        log.debug("✅ Generation progress updated: historyId={}, step={}/{}", historyId, currentStep, totalSteps);
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

            // GPU 리소스 반환
            gpuResourceManager.release(historyId);

            // WebSocket으로 실패 이벤트 전송
            GenerationHistoryResponse history = generationService.getGenerationHistory(historyId);
            Map<String, Object> failureEvent = new HashMap<>();
            failureEvent.put("status", "FAILED");
            failureEvent.put("historyId", historyId);
            failureEvent.put("message", error);

            webSocketHandler.sendToUser(history.getUserId(), failureEvent);
        }
    }
}
