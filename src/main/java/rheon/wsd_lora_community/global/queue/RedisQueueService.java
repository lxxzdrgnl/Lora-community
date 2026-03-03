package rheon.wsd_lora_community.global.queue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis 작업 큐 서비스
 * - 학습/생성 작업을 Redis Queue로 관리
 * - FIFO 순서 보장 (List 구조)
 * - 작업 상태 추적 (Hash 구조)
 * - 타임아웃 체크 (Sorted Set 구조)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedisQueueService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    // Redis Key 패턴
    private static final String QUEUE_PENDING = "queue:%s:pending";         // List: 대기 중인 작업
    private static final String QUEUE_PROCESSING = "queue:%s:processing";   // Sorted Set: 처리 중인 작업 (timestamp)
    private static final String JOB_DETAIL = "job:%s:%s";                   // Hash: 작업 상세 정보

    // 작업 타입
    public enum JobType {
        TRAINING,
        GENERATION
    }

    /**
     * 작업을 큐에 추가
     *
     * @param jobType 작업 타입 (TRAINING/GENERATION)
     * @param jobId 작업 ID
     * @param jobData 작업 정보 (Map)
     */
    public void enqueue(JobType jobType, Long jobId, Map<String, Object> jobData) {
        String queueKey = String.format(QUEUE_PENDING, jobType.name().toLowerCase());
        String jobKey = String.format(JOB_DETAIL, jobType.name().toLowerCase(), jobId);

        try {
            // 작업 정보를 Hash에 저장
            jobData.put("jobId", jobId);
            jobData.put("enqueuedAt", System.currentTimeMillis());
            jobData.put("status", "PENDING");

            for (Map.Entry<String, Object> entry : jobData.entrySet()) {
                String value = entry.getValue() == null ? "" :
                               entry.getValue() instanceof String ? (String) entry.getValue() :
                               objectMapper.writeValueAsString(entry.getValue());
                redisTemplate.opsForHash().put(jobKey, entry.getKey(), value);
            }

            // 작업 ID를 큐에 추가 (RPUSH = 끝에 추가)
            redisTemplate.opsForList().rightPush(queueKey, jobId.toString());

            long queueSize = redisTemplate.opsForList().size(queueKey);
            log.info("📥 작업 큐에 추가: {} jobId={} (대기열: {}개)", jobType, jobId, queueSize);

        } catch (JsonProcessingException e) {
            log.error("❌ 작업 큐 추가 실패: {} jobId={}, error={}", jobType, jobId, e.getMessage());
            throw new RuntimeException("Failed to enqueue job", e);
        }
    }

    /**
     * 큐에서 작업 꺼내기 (블로킹, 최대 5초 대기)
     *
     * @param jobType 작업 타입
     * @return 작업 ID (null이면 큐가 비어있음)
     */
    public Long dequeue(JobType jobType) {
        String queueKey = String.format(QUEUE_PENDING, jobType.name().toLowerCase());

        // BLPOP: 블로킹 방식으로 큐에서 꺼내기 (최대 5초 대기)
        var result = redisTemplate.opsForList().leftPop(queueKey, 5, TimeUnit.SECONDS);

        if (result != null) {
            Long jobId = Long.parseLong(result);

            // processing Set에 추가 (타임아웃 체크용)
            String processingKey = String.format(QUEUE_PROCESSING, jobType.name().toLowerCase());
            redisTemplate.opsForZSet().add(processingKey, jobId.toString(), System.currentTimeMillis());

            // 작업 상태 업데이트
            String jobKey = String.format(JOB_DETAIL, jobType.name().toLowerCase(), jobId);
            redisTemplate.opsForHash().put(jobKey, "status", "PROCESSING");
            redisTemplate.opsForHash().put(jobKey, "startedAt", String.valueOf(System.currentTimeMillis()));

            log.info("✅ 작업 큐에서 꺼냄: {} jobId={}", jobType, jobId);
            return jobId;
        }

        return null;
    }

    /**
     * 작업 완료 처리
     *
     * @param jobType 작업 타입
     * @param jobId 작업 ID
     */
    public void complete(JobType jobType, Long jobId) {
        String processingKey = String.format(QUEUE_PROCESSING, jobType.name().toLowerCase());
        String jobKey = String.format(JOB_DETAIL, jobType.name().toLowerCase(), jobId);

        // processing Set에서 제거
        redisTemplate.opsForZSet().remove(processingKey, jobId.toString());

        // 작업 정보 삭제 (완료된 작업은 Redis에 보관하지 않음, DB에만 저장)
        redisTemplate.delete(jobKey);

        log.info("🔓 작업 완료 처리: {} jobId={}", jobType, jobId);
    }

    /**
     * 작업 실패 처리
     *
     * @param jobType 작업 타입
     * @param jobId 작업 ID
     */
    public void fail(JobType jobType, Long jobId) {
        String processingKey = String.format(QUEUE_PROCESSING, jobType.name().toLowerCase());
        String jobKey = String.format(JOB_DETAIL, jobType.name().toLowerCase(), jobId);

        // processing Set에서 제거
        redisTemplate.opsForZSet().remove(processingKey, jobId.toString());

        // 작업 정보 삭제
        redisTemplate.delete(jobKey);

        log.info("❌ 작업 실패 처리: {} jobId={}", jobType, jobId);
    }

    /**
     * 큐 상태 조회
     *
     * @param jobType 작업 타입
     * @return 큐 상태 (대기 중, 처리 중 작업 수)
     */
    public QueueStatus getQueueStatus(JobType jobType) {
        String pendingKey = String.format(QUEUE_PENDING, jobType.name().toLowerCase());
        String processingKey = String.format(QUEUE_PROCESSING, jobType.name().toLowerCase());

        Long pendingCount = redisTemplate.opsForList().size(pendingKey);
        Long processingCount = redisTemplate.opsForZSet().size(processingKey);

        return new QueueStatus(
                pendingCount != null ? pendingCount.intValue() : 0,
                processingCount != null ? processingCount.intValue() : 0
        );
    }

    /**
     * 타임아웃된 작업 조회 (30Input type (c10::Half) and bias type (float) should be the same분 이상 처리 중인 작업)
     *
     * @param jobType 작업 타입
     * @param timeoutMinutes 타임아웃 시간 (분)
     * @return 타임아웃된 작업 ID 목록
     */
    public Set<String> getTimeoutJobs(JobType jobType, int timeoutMinutes) {
        String processingKey = String.format(QUEUE_PROCESSING, jobType.name().toLowerCase());
        long cutoffTime = System.currentTimeMillis() - Duration.ofMinutes(timeoutMinutes).toMillis();

        // cutoffTime 이전에 시작된 작업들 조회
        return redisTemplate.opsForZSet().rangeByScore(processingKey, 0, cutoffTime);
    }

    /**
     * 작업 정보 조회
     *
     * @param jobType 작업 타입
     * @param jobId 작업 ID
     * @return 작업 정보 (Map)
     */
    public Map<String, String> getJobDetail(JobType jobType, Long jobId) {
        String jobKey = String.format(JOB_DETAIL, jobType.name().toLowerCase(), jobId);
        Map<Object, Object> rawData = redisTemplate.opsForHash().entries(jobKey);

        Map<String, String> result = new HashMap<>();
        rawData.forEach((key, value) -> result.put(key.toString(), value.toString()));

        return result;
    }

    /**
     * 큐 상태 DTO
     */
    public record QueueStatus(
            int pending,    // 대기 중인 작업 수
            int processing  // 처리 중인 작업 수
    ) {}
}
