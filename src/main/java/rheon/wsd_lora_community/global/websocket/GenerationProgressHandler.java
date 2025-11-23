package rheon.wsd_lora_community.global.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 이미지 생성 진행률 WebSocket 핸들러
 *
 * 클라이언트별로 WebSocket 세션을 관리하고, 생성 완료 시 해당 사용자에게만 알림을 전송합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GenerationProgressHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper;

    // userId -> WebSocketSession 매핑
    private final Map<Long, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 연결 시 userId 추출 (쿼리 파라미터에서)
        String query = session.getUri().getQuery();
        Long userId = extractUserIdFromQuery(query);

        if (userId != null) {
            sessions.put(userId, session);
            log.info("🔌 WebSocket 연결: userId={}, sessionId={}", userId, session.getId());
        } else {
            log.warn("⚠️ WebSocket 연결 실패: userId가 없음");
            session.close(CloseStatus.BAD_DATA);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // Ping 메시지 처리 (Heartbeat)
        try {
            String payload = message.getPayload();
            Map<String, Object> data = objectMapper.readValue(payload, Map.class);

            if ("ping".equals(data.get("type"))) {
                // Pong 응답 전송
                Map<String, String> pong = Map.of("type", "pong");
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(pong)));
                log.debug("💓 Ping 수신 → Pong 응답 전송: sessionId={}", session.getId());
            }
        } catch (Exception e) {
            log.warn("⚠️ 메시지 처리 실패: {}", e.getMessage());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        // 연결 종료 시 세션 제거
        Long userId = findUserIdBySession(session);
        if (userId != null) {
            sessions.remove(userId);
            log.info("🔌 WebSocket 연결 종료: userId={}, sessionId={}", userId, session.getId());
        }
    }

    /**
     * 특정 사용자에게 메시지 전송
     */
    public void sendToUser(Long userId, Object message) {
        WebSocketSession session = sessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                String json = objectMapper.writeValueAsString(message);
                session.sendMessage(new TextMessage(json));
                log.info("📤 WebSocket 메시지 전송: userId={}, message={}", userId, json);
            } catch (IOException e) {
                log.error("❌ WebSocket 메시지 전송 실패: userId={}, error={}", userId, e.getMessage());
            }
        } else {
            log.warn("⚠️ WebSocket 세션 없음: userId={}", userId);
        }
    }

    /**
     * 모든 연결된 사용자에게 메시지 브로드캐스트
     */
    public void broadcast(Object message) {
        String json;
        try {
            json = objectMapper.writeValueAsString(message);
        } catch (IOException e) {
            log.error("❌ JSON 변환 실패: {}", e.getMessage());
            return;
        }

        sessions.forEach((userId, session) -> {
            if (session.isOpen()) {
                try {
                    session.sendMessage(new TextMessage(json));
                } catch (IOException e) {
                    log.error("❌ 브로드캐스트 실패: userId={}, error={}", userId, e.getMessage());
                }
            }
        });
    }

    /**
     * 쿼리 파라미터에서 userId 추출
     * 예: ws://localhost:8080/ws/generation?userId=123
     */
    private Long extractUserIdFromQuery(String query) {
        if (query == null || query.isEmpty()) {
            return null;
        }

        for (String param : query.split("&")) {
            String[] keyValue = param.split("=");
            if (keyValue.length == 2 && "userId".equals(keyValue[0])) {
                try {
                    return Long.parseLong(keyValue[1]);
                } catch (NumberFormatException e) {
                    log.warn("⚠️ userId 파싱 실패: {}", keyValue[1]);
                    return null;
                }
            }
        }

        return null;
    }

    /**
     * 세션으로 userId 찾기
     */
    private Long findUserIdBySession(WebSocketSession session) {
        return sessions.entrySet().stream()
                .filter(entry -> entry.getValue().getId().equals(session.getId()))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }
}
