package rheon.wsd_lora_community.global.queue;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import rheon.wsd_lora_community.generation.entity.GeneratedImage;
import rheon.wsd_lora_community.generation.entity.GenerationHistory;
import rheon.wsd_lora_community.generation.repository.GenerationHistoryRepository;
import rheon.wsd_lora_community.training.entity.TrainingJob;
import rheon.wsd_lora_community.training.repository.TrainingJobRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Redis 기반 작업 복구 스케줄러
 * - 5분 이상 GENERATING/TRAINING 상태로 남아있는 작업을 Redis에서 확인 후 복구
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisRecoveryScheduler {

    private final GenerationHistoryRepository generationHistoryRepository;
    private final TrainingJobRepository trainingJobRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 5분마다 실행: GENERATING 상태로 3분 이상 남아있는 작업 복구
     * - updatedAt 대신 createdAt 기준으로 변경 (진행률 업데이트로 인한 updatedAt 갱신 방지)
     */
    @Scheduled(fixedDelay = 300000) // 5분마다
    @Transactional
    public void recoverStuckGenerations() {
        try {
            log.debug("🔍 [Recovery] Generation 복구 스케줄러 실행 시작");

            LocalDateTime threeMinutesAgo = LocalDateTime.now().minusMinutes(3);

            // GENERATING 상태인 작업 모두 찾기 (createdAt 기준)
            List<GenerationHistory> allGenerating = generationHistoryRepository
                    .findByStatusAndUpdatedAtBefore("GENERATING", threeMinutesAgo);

            log.debug("🔍 [Recovery] GENERATING 상태 작업 {}개 발견", allGenerating.size());

            if (allGenerating.isEmpty()) {
                log.debug("🔍 [Recovery] 복구할 작업 없음");
                return;
            }

            // 3분 이상 된 작업만 필터링
            List<GenerationHistory> stuckGenerations = allGenerating.stream()
                    .filter(h -> h.getCreatedAt().isBefore(threeMinutesAgo))
                    .toList();

            if (stuckGenerations.isEmpty()) {
                log.info("🔍 [Recovery] 3분 이상 된 작업 없음 (모두 최근 생성)");
                return;
            }

            log.warn("⚠️ [Recovery] 3분 이상 GENERATING 상태인 작업 {}개 발견", stuckGenerations.size());

            for (GenerationHistory history : stuckGenerations) {
                Long historyId = history.getId();
                LocalDateTime createdAt = history.getCreatedAt();
                LocalDateTime updatedAt = history.getUpdatedAt();

                log.info("🔍 [Recovery] 작업 확인 중: historyId={}, createdAt={}, updatedAt={}",
                    historyId, createdAt, updatedAt);

                // Redis에서 작업 상태 확인
                String jobKey = String.format("job:generation:%d", historyId);
                Map<Object, Object> redisData;

                try {
                    redisData = redisTemplate.opsForHash().entries(jobKey);
                    log.info("🔍 [Recovery] Redis 조회: key={}, dataSize={}", jobKey, redisData.size());
                } catch (Exception e) {
                    log.warn("⚠️ [Recovery] Redis 역직렬화 실패 (corrupted data): historyId={}, error={}",
                        historyId, e.getMessage());
                    // Redis 데이터가 손상된 경우 키 삭제 후 DB를 FAILED 처리
                    try {
                        redisTemplate.delete(jobKey);
                        log.info("🗑️ [Recovery] 손상된 Redis 키 삭제: {}", jobKey);
                    } catch (Exception deleteErr) {
                        log.error("❌ [Recovery] Redis 키 삭제 실패: {}", jobKey, deleteErr);
                    }
                    history.markAsFailed("3분 초과 타임아웃 (Redis 데이터 손상)");
                    generationHistoryRepository.save(history);
                    generationHistoryRepository.flush();
                    log.info("✅ [Recovery] FAILED 처리 완료 (corrupted data): historyId={}", historyId);
                    continue;
                }

                if (redisData.isEmpty()) {
                    log.warn("⚠️ [Recovery] Redis에 데이터 없음: historyId={}, DB를 FAILED 처리", historyId);
                    history.markAsFailed("3분 초과 타임아웃 (Redis 데이터 없음)");
                    generationHistoryRepository.save(history);
                    generationHistoryRepository.flush(); // 즉시 DB 반영
                    log.info("✅ [Recovery] FAILED 처리 완료: historyId={}", historyId);
                    continue;
                }

                String redisStatus = (String) redisData.get("status");
                log.info("🔍 [Recovery] Redis 확인: historyId={}, redisStatus={}, redisData={}",
                    historyId, redisStatus, redisData);

                if ("SUCCESS".equals(redisStatus)) {
                    // Redis에 SUCCESS 결과가 있음 → DB 업데이트
                    String imageUrlsJson = (String) redisData.get("imageUrls");
                    log.info("🔍 [Recovery] SUCCESS 감지: historyId={}, imageUrlsJson={}", historyId, imageUrlsJson);

                    if (imageUrlsJson != null && !imageUrlsJson.isEmpty()) {
                        try {
                            // JSON 파싱
                            List<String> imageS3Keys = parseJsonArray(imageUrlsJson);
                            log.info("🔍 [Recovery] 이미지 파싱 완료: historyId={}, imageCount={}", historyId, imageS3Keys.size());

                            // DB 업데이트 - GeneratedImage 객체 생성
                            int order = 1;
                            for (String s3Key : imageS3Keys) {
                                // S3 키를 URL로 변환
                                String s3Url = "https://lora-generated-image-bucket.s3.ap-northeast-2.amazonaws.com/" + s3Key;

                                GeneratedImage image = GeneratedImage.builder()
                                        .generationHistory(history)
                                        .s3Key(s3Key)
                                        .s3Url(s3Url)
                                        .displayOrder(order++)
                                        .build();

                                history.addGeneratedImage(image);
                            }
                            history.markAsSuccess();
                            generationHistoryRepository.save(history);
                            generationHistoryRepository.flush(); // 즉시 DB 반영

                            log.info("✅ [Recovery] Redis 복구 성공: historyId={}, images={}", historyId, imageS3Keys.size());
                        } catch (Exception e) {
                            log.error("❌ [Recovery] Redis 복구 실패: historyId={}, error={}", historyId, e.getMessage(), e);
                            history.markAsFailed("Redis 복구 실패: " + e.getMessage());
                            generationHistoryRepository.save(history);
                            generationHistoryRepository.flush();
                        }
                    } else {
                        log.warn("⚠️ [Recovery] Redis에 imageUrls 없음: historyId={}", historyId);
                        history.markAsFailed("Redis 결과 불완전 (imageUrls 없음)");
                        generationHistoryRepository.save(history);
                        generationHistoryRepository.flush();
                    }
                } else if ("FAILED".equals(redisStatus)) {
                    // Redis에 FAILED 상태 → DB도 FAILED 처리
                    String error = (String) redisData.get("error");
                    log.info("🔍 [Recovery] FAILED 감지: historyId={}, error={}", historyId, error);
                    history.markAsFailed(error != null ? error : "Redis에서 실패 상태 확인");
                    generationHistoryRepository.save(history);
                    generationHistoryRepository.flush();
                    log.info("✅ [Recovery] Redis 복구 (실패): historyId={}", historyId);
                } else {
                    // Redis에도 여전히 GENERATING → 타임아웃 처리
                    log.warn("⚠️ [Recovery] Redis에도 진행 중: historyId={}, redisStatus={}, 타임아웃 처리", historyId, redisStatus);
                    history.markAsFailed("3분 초과 타임아웃 (Redis: " + redisStatus + ")");
                    generationHistoryRepository.save(history);
                    generationHistoryRepository.flush();
                }
            }

        } catch (Exception e) {
            log.error("❌ Generation 복구 스케줄러 오류: {}", e.getMessage(), e);
        }
    }

    /**
     * 5분마다 실행: TRAINING 상태로 3분 이상 남아있는 작업 복구
     * - updatedAt 대신 createdAt 기준으로 변경 (진행률 업데이트로 인한 updatedAt 갱신 방지)
     */
    @Scheduled(fixedDelay = 300000) // 5분마다
    @Transactional
    public void recoverStuckTrainings() {
        try {
            log.debug("🔍 [Recovery] Training 복구 스케줄러 실행 시작");

            LocalDateTime threeMinutesAgo = LocalDateTime.now().minusMinutes(3);

            // TRAINING 상태인 작업 모두 찾기
            List<TrainingJob> allTraining = trainingJobRepository
                    .findByStatusAndUpdatedAtBefore(TrainingJob.TrainingStatus.TRAINING, threeMinutesAgo);

            log.debug("🔍 [Recovery] TRAINING 상태 작업 {}개 발견", allTraining.size());

            if (allTraining.isEmpty()) {
                log.debug("🔍 [Recovery] 복구할 작업 없음");
                return;
            }

            // 3분 이상 된 작업만 필터링
            List<TrainingJob> stuckTrainings = allTraining.stream()
                    .filter(job -> job.getCreatedAt().isBefore(threeMinutesAgo))
                    .toList();

            if (stuckTrainings.isEmpty()) {
                log.info("🔍 [Recovery] 3분 이상 된 작업 없음 (모두 최근 생성)");
                return;
            }

            log.warn("⚠️ [Recovery] 3분 이상 TRAINING 상태인 작업 {}개 발견", stuckTrainings.size());

            for (TrainingJob job : stuckTrainings) {
                Long jobId = job.getId();
                LocalDateTime createdAt = job.getCreatedAt();
                LocalDateTime updatedAt = job.getUpdatedAt();

                log.info("🔍 [Recovery] 작업 확인 중: jobId={}, createdAt={}, updatedAt={}",
                    jobId, createdAt, updatedAt);

                // Redis에서 작업 상태 확인
                String jobKey = String.format("job:training:%d", jobId);
                Map<Object, Object> redisData;

                try {
                    redisData = redisTemplate.opsForHash().entries(jobKey);
                    log.info("🔍 [Recovery] Redis 조회: key={}, dataSize={}", jobKey, redisData.size());
                } catch (Exception e) {
                    log.warn("⚠️ [Recovery] Redis 역직렬화 실패 (corrupted data): jobId={}, error={}",
                        jobId, e.getMessage());
                    // Redis 데이터가 손상된 경우 키 삭제 후 DB를 FAILED 처리
                    try {
                        redisTemplate.delete(jobKey);
                        log.info("🗑️ [Recovery] 손상된 Redis 키 삭제: {}", jobKey);
                    } catch (Exception deleteErr) {
                        log.error("❌ [Recovery] Redis 키 삭제 실패: {}", jobKey, deleteErr);
                    }
                    job.fail("3분 초과 타임아웃 (Redis 데이터 손상)");
                    trainingJobRepository.save(job);
                    trainingJobRepository.flush();
                    log.info("✅ [Recovery] FAILED 처리 완료 (corrupted data): jobId={}", jobId);
                    continue;
                }

                if (redisData.isEmpty()) {
                    log.warn("⚠️ [Recovery] Redis에 데이터 없음: jobId={}, DB를 FAILED 처리", jobId);
                    job.fail("3분 초과 타임아웃 (Redis 데이터 없음)");
                    trainingJobRepository.save(job);
                    trainingJobRepository.flush(); // 즉시 DB 반영
                    log.info("✅ [Recovery] FAILED 처리 완료: jobId={}", jobId);
                    continue;
                }

                String redisStatus = (String) redisData.get("status");
                log.info("🔍 [Recovery] Redis 확인: jobId={}, redisStatus={}, redisData={}",
                    jobId, redisStatus, redisData);

                if ("SUCCESS".equals(redisStatus)) {
                    // Redis에 SUCCESS 결과가 있음 → DB 업데이트
                    String s3ModelKey = (String) redisData.get("s3ModelKey");
                    log.info("🔍 [Recovery] SUCCESS 감지: jobId={}, s3ModelKey={}", jobId, s3ModelKey);

                    if (s3ModelKey != null && !s3ModelKey.isEmpty()) {
                        try {
                            // TrainingJob만 complete() 처리
                            // LoraModel 생성은 별도 처리 필요 (수동 또는 JobCallbackService 사용)
                            job.complete();
                            trainingJobRepository.save(job);
                            trainingJobRepository.flush(); // 즉시 DB 반영

                            log.info("✅ [Recovery] Redis 복구 성공: jobId={}, s3Key={} (LoraModel은 수동 생성 필요)", jobId, s3ModelKey);
                        } catch (Exception e) {
                            log.error("❌ [Recovery] Redis 복구 실패: jobId={}, error={}", jobId, e.getMessage(), e);
                            job.fail("Redis 복구 실패: " + e.getMessage());
                            trainingJobRepository.save(job);
                            trainingJobRepository.flush();
                        }
                    } else {
                        log.warn("⚠️ [Recovery] Redis에 s3ModelKey 없음: jobId={}", jobId);
                        job.fail("Redis 결과 불완전 (s3ModelKey 없음)");
                        trainingJobRepository.save(job);
                        trainingJobRepository.flush();
                    }
                } else if ("FAILED".equals(redisStatus)) {
                    // Redis에 FAILED 상태 → DB도 FAILED 처리
                    String error = (String) redisData.get("error");
                    log.info("🔍 [Recovery] FAILED 감지: jobId={}, error={}", jobId, error);
                    job.fail(error != null ? error : "Redis에서 실패 상태 확인");
                    trainingJobRepository.save(job);
                    trainingJobRepository.flush();
                    log.info("✅ [Recovery] Redis 복구 (실패): jobId={}", jobId);
                } else {
                    // Redis에도 여전히 진행 중 → 타임아웃 처리
                    log.warn("⚠️ [Recovery] Redis에도 진행 중: jobId={}, redisStatus={}, 타임아웃 처리", jobId, redisStatus);
                    job.fail("3분 초과 타임아웃 (Redis: " + redisStatus + ")");
                    trainingJobRepository.save(job);
                    trainingJobRepository.flush();
                }
            }

        } catch (Exception e) {
            log.error("❌ Training 복구 스케줄러 오류: {}", e.getMessage(), e);
        }
    }

    /**
     * JSON 배열 문자열을 List<String>으로 파싱
     */
    private List<String> parseJsonArray(String json) {
        // 간단한 JSON 배열 파싱 (예: ["url1","url2"])
        json = json.trim();
        if (json.startsWith("[") && json.endsWith("]")) {
            json = json.substring(1, json.length() - 1);
            if (json.isEmpty()) {
                return List.of();
            }
            return Arrays.stream(json.split(","))
                    .map(s -> s.trim().replaceAll("^\"|\"$", ""))
                    .toList();
        }
        return List.of();
    }
}
