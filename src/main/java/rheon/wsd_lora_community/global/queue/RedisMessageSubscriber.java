package rheon.wsd_lora_community.global.queue;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import rheon.wsd_lora_community.global.sse.SseEmitterService;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis Pub/Sub 메시지 리스너 (하이브리드 방식 + SSE)
 * - FastAPI에서 publish한 진행률 메시지를 실시간으로 수신
 * - SSE(Server-Sent Events)로 프론트엔드에 즉시 전달 (Pub/Sub)
 * - Redis에 진행 상황 저장 (Caching, TTL 1시간)
 *
 * 구독 채널:
 * - job:training:progress - 학습 진행률
 * - job:generation:progress - 이미지 생성 진행률
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisMessageSubscriber implements MessageListener {

    private final SseEmitterService sseEmitterService;
    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    // Redis 키 TTL (1시간)
    private static final Duration PROGRESS_TTL = Duration.ofHours(1);

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String channel = new String(message.getChannel());
            String payload = new String(message.getBody());

            log.info("📨 [Pub/Sub] 메시지 수신: channel={}, payload={}", channel, payload);

            // JSON 파싱
            @SuppressWarnings("unchecked")
            Map<String, Object> data = objectMapper.readValue(payload, Map.class);

            // 채널별 처리
            if (channel.startsWith("job:training:progress")) {
                handleTrainingProgress(data);
            } else if (channel.startsWith("job:generation:progress")) {
                handleGenerationProgress(data);
            } else {
                log.warn("⚠️ [Pub/Sub] 알 수 없는 채널: {}", channel);
            }

        } catch (Exception e) {
            log.error("❌ [Pub/Sub] 메시지 처리 실패: {}", e.getMessage(), e);
        }
    }

    /**
     * 학습 진행률 메시지 처리 (하이브리드 방식)
     *
     * 예상 메시지 형식:
     * {
     *   "jobId": 123,
     *   "userId": 1,
     *   "status": "TRAINING",
     *   "currentEpoch": 50,
     *   "totalEpochs": 100,
     *   "message": "Training 50/100",
     *   "phase": "TRAINING"
     * }
     */
    private void handleTrainingProgress(Map<String, Object> data) {
        try {
            Long userId = getLongValue(data, "userId");
            Long jobId = getLongValue(data, "jobId");
            String status = (String) data.get("status");
            Integer currentEpoch = getIntegerValue(data, "currentEpoch");
            Integer totalEpochs = getIntegerValue(data, "totalEpochs");
            String message = (String) data.get("message");
            String phase = (String) data.get("phase");

            if (userId == null) {
                log.warn("⚠️ [Pub/Sub] userId가 없습니다: {}", data);
                return;
            }

            // WebSocket으로 전송할 이벤트 생성
            Map<String, Object> progressEvent = new HashMap<>();
            progressEvent.put("type", "training_progress");
            progressEvent.put("jobId", jobId);
            progressEvent.put("status", status != null ? status : phase);
            progressEvent.put("currentEpoch", currentEpoch);
            progressEvent.put("totalEpochs", totalEpochs);
            progressEvent.put("message", message);
            progressEvent.put("phase", phase);

            // 진행률 계산 (퍼센트)
            if (currentEpoch != null && totalEpochs != null && totalEpochs > 0) {
                int progress = (int) ((currentEpoch * 100.0) / totalEpochs);
                progressEvent.put("progress", progress);
            }

            // Redis 캐싱은 FastAPI에서 이미 처리하므로 Spring Boot에서는 생략 (중복 방지)

            // SSE로 실시간 전송
            sseEmitterService.sendToUser(userId, "training_progress", progressEvent);

            log.info("✅ [SSE] Training 진행률 처리: userId={}, jobId={}, epoch={}/{}",
                    userId, jobId, currentEpoch, totalEpochs);

        } catch (Exception e) {
            log.error("❌ [Pub/Sub] Training 진행률 처리 실패: {}", e.getMessage(), e);
        }
    }

    /**
     * 이미지 생성 진행률 메시지 처리 (하이브리드 방식)
     *
     * 예상 메시지 형식:
     * {
     *   "historyId": 456,
     *   "userId": 1,
     *   "status": "GENERATING",
     *   "currentStep": 25,
     *   "totalSteps": 50,
     *   "message": "Generating 25/50"
     * }
     */
    private void handleGenerationProgress(Map<String, Object> data) {
        try {
            Long userId = getLongValue(data, "userId");
            Long historyId = getLongValue(data, "historyId");
            String status = (String) data.get("status");
            Integer currentStep = getIntegerValue(data, "currentStep");
            Integer totalSteps = getIntegerValue(data, "totalSteps");
            String message = (String) data.get("message");

            if (userId == null) {
                log.warn("⚠️ [Pub/Sub] userId가 없습니다: {}", data);
                return;
            }

            // WebSocket으로 전송할 이벤트 생성
            Map<String, Object> progressEvent = new HashMap<>();
            progressEvent.put("type", "generation_progress");
            progressEvent.put("historyId", historyId);
            progressEvent.put("status", status != null ? status : "GENERATING");
            progressEvent.put("currentStep", currentStep);
            progressEvent.put("totalSteps", totalSteps);
            progressEvent.put("message", message);

            // 진행률 계산 (퍼센트)
            if (currentStep != null && totalSteps != null && totalSteps > 0) {
                int progress = (int) ((currentStep * 100.0) / totalSteps);
                progressEvent.put("progress", progress);
            }

            // Redis 캐싱은 FastAPI에서 이미 처리하므로 Spring Boot에서는 생략 (중복 방지)

            // SSE로 실시간 전송
            sseEmitterService.sendToUser(userId, "generation_progress", progressEvent);

            log.info("✅ [SSE] Generation 진행률 처리: userId={}, historyId={}, step={}/{}",
                    userId, historyId, currentStep, totalSteps);

        } catch (Exception e) {
            log.error("❌ [Pub/Sub] Generation 진행률 처리 실패: {}", e.getMessage(), e);
        }
    }

    /**
     * Map에서 Long 값 추출 (다양한 타입 지원)
     */
    private Long getLongValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * Map에서 Integer 값 추출 (다양한 타입 지원)
     */
    private Integer getIntegerValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}
