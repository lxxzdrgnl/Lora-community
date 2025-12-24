package rheon.wsd_lora_community.global.security;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 커스텀 OAuth2 인증 요청 리졸버
 * - 모바일 지원을 위한 추가 파라미터 처리
 * - 동적 리디렉션을 위해 Referer를 state 파라미터에 포함
 * - CloudFront 환경에서 redirect_uri 정확히 설정
 */
@Slf4j
@Component
public class CustomAuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

    private final DefaultOAuth2AuthorizationRequestResolver defaultResolver;

    public CustomAuthorizationRequestResolver(ClientRegistrationRepository clientRegistrationRepository) {
        this.defaultResolver = new DefaultOAuth2AuthorizationRequestResolver(
                clientRegistrationRepository,
                "/oauth2/authorization"
        );
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        OAuth2AuthorizationRequest authorizationRequest = defaultResolver.resolve(request);
        return customizeAuthorizationRequest(authorizationRequest, request);
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
        OAuth2AuthorizationRequest authorizationRequest = defaultResolver.resolve(request, clientRegistrationId);
        return customizeAuthorizationRequest(authorizationRequest, request);
    }

    private OAuth2AuthorizationRequest customizeAuthorizationRequest(
            OAuth2AuthorizationRequest authorizationRequest,
            HttpServletRequest request
    ) {
        if (authorizationRequest == null) {
            return null;
        }

        // ✅ CloudFront/ngrok 헤더로부터 정확한 baseUrl 계산
        String scheme = request.getHeader("X-Forwarded-Proto");
        if (scheme == null) {
            scheme = request.getScheme();
        }

        String host = request.getHeader("X-Forwarded-Host");
        if (host == null) {
            host = request.getServerName();
            int port = request.getServerPort();
            if ((scheme.equals("http") && port != 80) || (scheme.equals("https") && port != 443)) {
                host = host + ":" + port;
            }
        }

        // CloudFront/ngrok 환경이면 강제로 HTTPS 사용
        if (host != null && (host.contains("cloudfront.net") || host.contains("ngrok"))) {
            scheme = "https";
        }

        String baseUrl = scheme + "://" + host;
        String correctRedirectUri = baseUrl + "/login/oauth2/code/google";
        log.info("🔗 OAuth2 redirect_uri: {}", correctRedirectUri);

        Map<String, Object> additionalParameters = new HashMap<>(authorizationRequest.getAdditionalParameters());

        // 프론트엔드에서 전달된 prompt 파라미터 처리
        String prompt = request.getParameter("prompt");

        // User-Agent로 모바일 감지
        String userAgent = request.getHeader("User-Agent");
        boolean isMobile = userAgent != null &&
                (userAgent.toLowerCase().contains("mobile") ||
                 userAgent.toLowerCase().contains("android") ||
                 userAgent.toLowerCase().contains("iphone") ||
                 userAgent.toLowerCase().contains("ipad"));

        // 모바일이거나 prompt 파라미터가 있는 경우
        if (isMobile || (prompt != null && !prompt.isEmpty())) {
            // 계정 선택 화면 강제 (Google 앱 리다이렉션 방지)
            additionalParameters.put("prompt", "select_account");
        }

        // refresh token을 받기 위해 access_type=offline 추가
        additionalParameters.put("access_type", "offline");

        // mobile_redirect 파라미터 또는 Referer 헤더를 state 파라미터에 인코딩
        String mobileRedirect = request.getParameter("mobile_redirect");
        String referer = request.getHeader("Referer");
        String originalState = authorizationRequest.getState();
        String newState = originalState;

        log.info("🔍 OAuth2 Authorization Request:");
        log.info("  - mobile_redirect parameter: {}", mobileRedirect);
        log.info("  - Referer header: {}", referer);
        log.info("  - Original state: {}", originalState);

        // 우선순위: mobile_redirect > Referer
        if (mobileRedirect != null && !mobileRedirect.isEmpty()) {
            String encodedMobileRedirect = URLEncoder.encode(mobileRedirect, StandardCharsets.UTF_8);
            newState = originalState + "::" + encodedMobileRedirect;
            log.info("✅ Using mobile_redirect in state: {}", newState);
        } else if (referer != null && !referer.isEmpty()) {
            String encodedReferer = URLEncoder.encode(referer, StandardCharsets.UTF_8);
            newState = originalState + "::" + encodedReferer;
            log.info("✅ Using Referer in state: {}", newState);
        } else {
            log.warn("⚠️ No mobile_redirect or Referer found, using original state only");
        }

        return OAuth2AuthorizationRequest.from(authorizationRequest)
                .additionalParameters(additionalParameters)
                .state(newState) // 커스텀 state 설정
                .redirectUri(correctRedirectUri) // ✅ 올바른 redirect_uri 명시적 설정
                .build();
    }
}
