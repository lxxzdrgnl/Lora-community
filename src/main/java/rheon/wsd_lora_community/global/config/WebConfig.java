package rheon.wsd_lora_community.global.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 설정
 * - CORS 설정 (멀티 인스턴스 환경 고려)
 * - 정적 리소스 핸들러
 * - 요청/응답 로깅 인터셉터
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final RequestLoggingInterceptor requestLoggingInterceptor;

    @Value("${cors.allowed-origins}")
    private String[] allowedOrigins;

    @Value("${cors.allowed-methods}")
    private String allowedMethods;

    @Value("${cors.allowed-headers}")
    private String allowedHeaders;

    @Value("${cors.allow-credentials}")
    private boolean allowCredentials;

    @Value("${cors.max-age}")
    private long maxAge;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")  // allowedOrigins 대신 allowedOriginPatterns 사용
                .allowedMethods(allowedMethods.split(","))
                .allowedHeaders(allowedHeaders)
                .allowCredentials(allowCredentials)
                .maxAge(maxAge);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 정적 파일 서빙 (이미지 등)
        registry.addResourceHandler("/static/**")
                .addResourceLocations("file:./uploads/");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 요청/응답 로깅 인터셉터 등록
        registry.addInterceptor(requestLoggingInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/swagger-ui/**", "/v3/api-docs/**", "/static/**");
    }
}
