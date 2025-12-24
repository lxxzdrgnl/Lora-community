package rheon.wsd_lora_community.global.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 요청/응답 로깅 인터셉터
 * - 요청 메서드, 경로, 상태코드, 지연시간 로그
 */
@Slf4j
@Component
public class RequestLoggingInterceptor implements HandlerInterceptor {

    private static final String START_TIME_ATTRIBUTE = "startTime";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        long startTime = System.currentTimeMillis();
        request.setAttribute(START_TIME_ATTRIBUTE, startTime);

        // 요청 로그: [메서드] 경로
        log.info("[REQUEST] {} {}", request.getMethod(), request.getRequestURI());

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        Long startTime = (Long) request.getAttribute(START_TIME_ATTRIBUTE);
        if (startTime == null) {
            return;
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // 응답 로그: [메서드] 경로 - 상태코드 (지연시간ms)
        log.info("[RESPONSE] {} {} - {} ({}ms)",
                request.getMethod(),
                request.getRequestURI(),
                response.getStatus(),
                duration
        );

        // 에러 발생 시 스택트레이스 로그
        if (ex != null) {
            log.error("[ERROR] {} {} - Exception occurred",
                    request.getMethod(),
                    request.getRequestURI(),
                    ex
            );
        }
    }
}
