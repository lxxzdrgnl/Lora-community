package rheon.wsd_lora_community.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import rheon.wsd_lora_community.global.dto.ErrorResponse;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Rate Limit 필터
 * - IP 기반 요청 제한
 * - 1분당 최대 100개 요청
 */
@Slf4j
@Component
public class RateLimitFilter implements Filter {

    private static final int MAX_REQUESTS_PER_MINUTE = 100;
    private static final long WINDOW_SIZE_MS = 60_000; // 1 minute

    // IP별 요청 카운터 (IP -> [count, timestamp])
    private final Map<String, RequestCounter> requestCounts = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String clientIp = getClientIp(httpRequest);

        // Rate limit 체크
        if (isRateLimitExceeded(clientIp)) {
            log.warn("[RATE LIMIT] IP {} exceeded rate limit", clientIp);
            sendRateLimitError(httpRequest, httpResponse);
            return;
        }

        chain.doFilter(request, response);
    }

    /**
     * Rate limit 초과 여부 확인
     */
    private boolean isRateLimitExceeded(String clientIp) {
        long currentTime = System.currentTimeMillis();

        RequestCounter counter = requestCounts.computeIfAbsent(clientIp, k -> new RequestCounter());

        synchronized (counter) {
            // 윈도우 시간이 지나면 초기화
            if (currentTime - counter.windowStart > WINDOW_SIZE_MS) {
                counter.reset(currentTime);
            }

            counter.increment();
            return counter.getCount() > MAX_REQUESTS_PER_MINUTE;
        }
    }

    /**
     * 클라이언트 IP 주소 추출 (프록시 고려)
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // X-Forwarded-For는 "client, proxy1, proxy2" 형식이므로 첫 번째 IP만 추출
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    /**
     * Rate limit 에러 응답 전송
     */
    private void sendRateLimitError(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ErrorResponse errorResponse = new ErrorResponse(
                request.getRequestURI(),
                HttpStatus.TOO_MANY_REQUESTS.value(),
                "RATE_LIMIT_EXCEEDED",
                "요청 제한을 초과했습니다. 잠시 후 다시 시도해주세요."
        );

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }

    /**
     * 요청 카운터 클래스
     */
    private static class RequestCounter {
        private final AtomicInteger count = new AtomicInteger(0);
        private long windowStart = System.currentTimeMillis();

        public void increment() {
            count.incrementAndGet();
        }

        public int getCount() {
            return count.get();
        }

        public void reset(long newWindowStart) {
            count.set(0);
            windowStart = newWindowStart;
        }
    }
}
