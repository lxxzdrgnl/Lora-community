package rheon.wsd_lora_community.global.sse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SSE(Server-Sent Events) Emitter 관리 서비스
 * - userId별로 SseEmitter 관리
 * - Redis Pub/Sub 메시지를 SSE로 전송
 */
@Slf4j
@Service
public class SseEmitterService {

    // userId -> SseEmitter 매핑 (동시성 처리)
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    /**
     * SSE 연결 생성
     *
     * @param userId 사용자 ID
     * @return SseEmitter
     */
    public SseEmitter createEmitter(Long userId) {
        // SSE 타임아웃: 30분 (학습은 길 수 있음)
        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L);

        // 연결 시 더미 이벤트 전송 (연결 확인용)
        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data("SSE Connected for userId: " + userId));
            log.info("✅ SSE 연결 생성: userId={}", userId);
        } catch (IOException e) {
            log.error("❌ SSE 초기 메시지 전송 실패: userId={}", userId, e);
            return emitter;
        }

        // Emitter 저장
        emitters.put(userId, emitter);

        // 완료/타임아웃/에러 시 Emitter 제거
        emitter.onCompletion(() -> {
            log.info("🔌 SSE 연결 완료: userId={}", userId);
            emitters.remove(userId);
        });

        emitter.onTimeout(() -> {
            log.warn("⏰ SSE 타임아웃: userId={}", userId);
            emitters.remove(userId);
            emitter.complete();
        });

        emitter.onError((ex) -> {
            log.error("❌ SSE 에러: userId={}", userId, ex);
            emitters.remove(userId);
        });

        return emitter;
    }

    /**
     * 특정 사용자에게 SSE 메시지 전송
     *
     * @param userId 사용자 ID
     * @param eventName 이벤트 이름 (예: training_progress, generation_progress)
     * @param data 전송할 데이터
     */
    public void sendToUser(Long userId, String eventName, Object data) {
        SseEmitter emitter = emitters.get(userId);

        if (emitter == null) {
            log.debug("⚠️ SSE Emitter 없음 (사용자가 연결 안 함): userId={}", userId);
            return;
        }

        try {
            emitter.send(SseEmitter.event()
                    .name(eventName)
                    .data(data));
            log.debug("📤 SSE 메시지 전송: userId={}, event={}", userId, eventName);
        } catch (IOException e) {
            log.error("❌ SSE 메시지 전송 실패: userId={}, event={}", userId, eventName, e);
            // 전송 실패 시 Emitter 제거
            emitters.remove(userId);
            emitter.completeWithError(e);
        }
    }

    /**
     * 특정 사용자의 SSE 연결 종료
     *
     * @param userId 사용자 ID
     */
    public void completeEmitter(Long userId) {
        SseEmitter emitter = emitters.remove(userId);
        if (emitter != null) {
            emitter.complete();
            log.info("🔌 SSE 연결 종료: userId={}", userId);
        }
    }

    /**
     * 연결된 사용자 수 확인
     *
     * @return 연결된 사용자 수
     */
    public int getConnectedCount() {
        return emitters.size();
    }
}
