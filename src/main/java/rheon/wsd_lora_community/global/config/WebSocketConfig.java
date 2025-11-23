package rheon.wsd_lora_community.global.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;
import rheon.wsd_lora_community.global.websocket.GenerationProgressHandler;

/**
 * WebSocket 설정
 *
 * 이미지 생성 진행률을 실시간으로 클라이언트에게 전송하기 위한 WebSocket 엔드포인트 설정
 */
@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final GenerationProgressHandler generationProgressHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(generationProgressHandler, "/ws/generation")
                .setAllowedOrigins("*");  // CORS 허용 (프로덕션에서는 특정 도메인만)
    }

    /**
     * WebSocket 세션 타임아웃 설정
     * - maxSessionIdleTimeout: 세션이 idle 상태로 유지될 수 있는 최대 시간 (밀리초)
     * - maxTextMessageBufferSize: 텍스트 메시지 버퍼 크기
     * - maxBinaryMessageBufferSize: 바이너리 메시지 버퍼 크기
     */
    @Bean
    public ServletServerContainerFactoryBean createWebSocketContainer() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        container.setMaxSessionIdleTimeout(600000L); // 10분 (600,000 ms)
        container.setMaxTextMessageBufferSize(8192);
        container.setMaxBinaryMessageBufferSize(8192);
        return container;
    }
}
