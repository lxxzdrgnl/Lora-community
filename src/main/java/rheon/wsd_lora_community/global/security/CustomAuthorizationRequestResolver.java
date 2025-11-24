package rheon.wsd_lora_community.global.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import java.util.HashMap;
import java.util.Map;

/**
 * 커스텀 OAuth2 인증 요청 리졸버
 * - 모바일 지원을 위한 추가 파라미터 처리
 */
@Component
public class CustomAuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

    private final DefaultOAuth2AuthorizationRequestResolver defaultResolver;
    private final String callbackUrlBase;


    public CustomAuthorizationRequestResolver(ClientRegistrationRepository clientRegistrationRepository, @Value("${app.callback-url}") String callbackUrlBase) {
        this.defaultResolver = new DefaultOAuth2AuthorizationRequestResolver(
                clientRegistrationRepository,
                "/oauth2/authorization"
        );
        this.callbackUrlBase = callbackUrlBase;
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

        // 동적 redirect_uri 생성 (devtunnel 지원)
        String redirectUri = getRedirectUri(request);

        return OAuth2AuthorizationRequest.from(authorizationRequest)
                .additionalParameters(additionalParameters)
                .redirectUri(redirectUri)
                .build();
    }

    /**
     * 요청 헤더에서 동적으로 redirect_uri 생성
     */
    private String getRedirectUri(HttpServletRequest request) {
        // 1. Referer 헤더에서 origin 추출
        String referer = request.getHeader("Referer");
        String origin;

        if (referer != null && referer.contains("devtunnels.ms")) {
            // Referer에서 origin 추출
            try {
                java.net.URI uri = new java.net.URI(referer);
                origin = uri.getScheme() + "://" + uri.getAuthority();
            } catch (Exception e) {
                origin = callbackUrlBase;
            }
        } else {
            // 2. 기본값: 현재 요청의 origin
            String scheme = request.getScheme();
            String serverName = request.getServerName();
            int serverPort = request.getServerPort();

            origin = scheme + "://" + serverName;
            if ((scheme.equals("http") && serverPort != 80) ||
                (scheme.equals("https") && serverPort != 443)) {
                origin += ":" + serverPort;
            }
        }

        return origin + "/login/oauth2/code/google";
    }
}
