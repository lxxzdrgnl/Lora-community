package rheon.wsd_lora_community.global.config;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value; // [중요] 설정값 읽기용
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration; // [중요] CORS 설정용
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import rheon.wsd_lora_community.global.security.CustomAuthorizationRequestResolver;
import rheon.wsd_lora_community.global.security.JwtAuthenticationFilter;
import rheon.wsd_lora_community.global.security.OAuth2SuccessHandler;

import java.util.Arrays;
import java.util.List;

/**
 * Spring Security 설정
 * - JWT 기반 Stateless 인증
 * - OAuth2 Google 로그인
 * - CORS 설정 (application.yml 연동)
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final CustomAuthorizationRequestResolver customAuthorizationRequestResolver;

    // ▼▼▼ [추가됨] application.yml의 설정을 읽어오는 변수들 ▼▼▼
    @Value("${cors.allowed-origins}")
    private String allowedOrigins;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // ▼▼▼ [수정됨] CORS 설정을 구체적인 소스(corsConfigurationSource)와 연결 ▼▼▼
                // 기존: .cors(cors -> cors.configure(http)) <- 이건 설정을 안 읽음!
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // CSRF 비활성화 (JWT 사용으로 불필요)
                .csrf(csrf -> csrf.disable())

                // 세션 사용 안 함 (Stateless)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 요청별 인증 설정
                .authorizeHttpRequests(auth -> auth
                        // 보안 취약점 스캔 차단 (Vendor 경로 등)
                        // ⚠️ /api/admin은 제외 (관리자 API는 인증 필요)
                        .requestMatchers(
                                "/vendor/**",
                                "/phpunit/**",
                                "/wp-admin/**",
                                "/wp-content/**",
                                "/phpmyadmin/**",
                                "/*.php"  // 루트 레벨 PHP 파일만 차단
                        ).denyAll()
                        .requestMatchers(
                                "/",
                                "/api/auth/**",  // 모든 인증 엔드포인트 (/api/auth/google/token 포함)
                                "/login/oauth2/**",
                                "/oauth2/**",
                                "/h2-console/**",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/api-docs/**",
                                "/swagger-ui.html"
                        ).permitAll()
                        .requestMatchers(
                                "/api/models",
                                "/api/models/{modelId}",
                                "/api/models/{modelId}/samples",
                                "/api/models/{modelId}/prompts",
                                "/api/models/{modelId}/comments",
                                "/api/tags",
                                "/api/tags/popular",
                                "/api/search/**",
                                "/api/training/callback",  // FastAPI 학습 콜백
                                "/api/generate/history"    // FastAPI 생성 콜백
                        ).permitAll()
                        // 관리자 API는 ROLE_ADMIN 권한 필요 (Controller @PreAuthorize에서 체크)
                        .requestMatchers("/api/admin/**").authenticated()
                        .anyRequest().authenticated()
                )

                // OAuth2 로그인 설정
                .oauth2Login(oauth2 -> oauth2
                        .authorizationEndpoint(authorization -> authorization
                                .baseUri("/oauth2/authorization")
                                .authorizationRequestResolver(customAuthorizationRequestResolver)
                        )
                        .redirectionEndpoint(redirection -> redirection
                                .baseUri("/login/oauth2/code/*")
                        )
                        .successHandler(oAuth2SuccessHandler)
                )

                // 예외 처리 (API 인증 실패 시 401 반환, OAuth2 리다이렉트 방지)
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            String requestUri = request.getRequestURI();
                            // API 요청은 OAuth2 리다이렉트 방지 (401 JSON 반환)
                            if (requestUri.startsWith("/api/")) {
                                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                response.setContentType("application/json;charset=UTF-8");
                                response.getWriter().write("{\"success\":false,\"message\":\"인증이 필요합니다.\",\"code\":\"UNAUTHORIZED\"}");
                            } else {
                                // 브라우저 요청은 OAuth2 로그인 페이지로 리다이렉트
                                response.sendRedirect("/oauth2/authorization/google");
                            }
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            // Access Denied 에러는 403 JSON 반환
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write("{\"success\":false,\"message\":\"접근 권한이 없습니다.\",\"code\":\"FORBIDDEN\"}");
                        })
                )

                // JWT 필터 추가
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                // H2 Console 설정
                .headers(headers -> headers
                        .frameOptions(frameOptions -> frameOptions.disable())
                );

        return http.build();
    }

    // ▼▼▼ [추가됨] CORS 설정을 실제로 적용하는 빈 (이게 없어서 안 됐던 것!) ▼▼▼
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 1. yml에서 가져온 origin을 패턴으로 설정 (allowCredentials=true와 호환)
        List<String> origins = Arrays.asList(allowedOrigins.split(","));
        configuration.setAllowedOriginPatterns(origins);

        // 2. 허용할 HTTP 메서드 (OPTIONS, POST 필수)
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));

        // 3. 허용할 헤더
        configuration.setAllowedHeaders(Arrays.asList("*"));

        // 4. 자격 증명 허용 (쿠키, Authorization 헤더)
        configuration.setAllowCredentials(true);

        // 5. 프론트엔드에서 헤더를 읽을 수 있게 허용 (자동 갱신된 토큰 포함)
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Set-Cookie", "X-New-Access-Token"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}