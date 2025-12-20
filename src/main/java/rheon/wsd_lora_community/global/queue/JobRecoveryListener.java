package rheon.wsd_lora_community.global.queue;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import rheon.wsd_lora_community.generation.service.GenerationService;
import rheon.wsd_lora_community.global.queue.RedisQueueService.JobType;
import rheon.wsd_lora_community.training.service.TrainingService;

import java.util.Set;

/**
 * 작업 복구 리스너
 * - 서버 시작 시 Redis에 남아있는 처리 중인 작업 복구
 * - 타임아웃된 작업은 FAILED 처리
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JobRecoveryListener {

    private final RedisQueueService queueService;
    private final TrainingService trainingService;
    private final GenerationService generationService;

    private static final int TIMEOUT_MINUTES = 30; // 30분 이상 처리 중이면 타임아웃

    /**
     * 서버 시작 완료 시 작업 복구
     */
    @EventListener(ApplicationReadyEvent.class)
    public void recoverJobs() {
        log.info("🔍 서버 시작: Redis 작업 복구 시작");

        try {
            recoverTrainingJobs();
            recoverGenerationJobs();
            log.info("✅ Redis 작업 복구 완료");
        } catch (Exception e) {
            log.error("❌ Redis 작업 복구 중 오류 발생: {}", e.getMessage(), e);
        }
    }

    /**
     * Training 작업 복구
     */
    private void recoverTrainingJobs() {
        // 타임아웃된 작업 조회 (30분 이상 처리 중인 작업)
        Set<String> timeoutJobs = queueService.getTimeoutJobs(JobType.TRAINING, TIMEOUT_MINUTES);

        if (timeoutJobs.isEmpty()) {
            log.info("✅ Training: 타임아웃된 작업 없음");
            return;
        }

        log.warn("⚠️ Training: 타임아웃된 작업 {}개 발견", timeoutJobs.size());

        for (String jobIdStr : timeoutJobs) {
            try {
                Long jobId = Long.parseLong(jobIdStr);

                // DB에서 FAILED 처리
                trainingService.failTraining(jobId, "서버 재시작 감지: 30분 이상 응답 없음 (타임아웃)");

                // Redis에서 제거
                queueService.fail(JobType.TRAINING, jobId);

                log.info("✅ Training 작업 타임아웃 처리: jobId={}", jobId);

            } catch (Exception e) {
                log.error("❌ Training 작업 복구 실패: jobId={}, error={}", jobIdStr, e.getMessage());
            }
        }
    }

    /**
     * Generation 작업 복구
     */
    private void recoverGenerationJobs() {
        // 타임아웃된 작업 조회 (30분 이상 처리 중인 작업)
        Set<String> timeoutJobs = queueService.getTimeoutJobs(JobType.GENERATION, TIMEOUT_MINUTES);

        if (timeoutJobs.isEmpty()) {
            log.info("✅ Generation: 타임아웃된 작업 없음");
            return;
        }

        log.warn("⚠️ Generation: 타임아웃된 작업 {}개 발견", timeoutJobs.size());

        for (String historyIdStr : timeoutJobs) {
            try {
                Long historyId = Long.parseLong(historyIdStr);

                // DB에서 FAILED 처리
                generationService.failGeneration(historyId, "서버 재시작 감지: 30분 이상 응답 없음 (타임아웃)");

                // Redis에서 제거
                queueService.fail(JobType.GENERATION, historyId);

                log.info("✅ Generation 작업 타임아웃 처리: historyId={}", historyId);

            } catch (Exception e) {
                log.error("❌ Generation 작업 복구 실패: historyId={}, error={}", historyIdStr, e.getMessage());
            }
        }
    }
}
