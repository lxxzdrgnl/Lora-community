package rheon.wsd_lora_community.global.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 커스텀 OAuth2 인증 요청 리졸버
 * - 모바일 지원을 위한 추가 파라미터 처리
 * - 동적 리디렉션을 위해 Referer를 state 파라미터에 포함
 */
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

        // Referer 헤더를 state 파라미터에 인코딩하여 추가
        String referer = request.getHeader("Referer");
        String originalState = authorizationRequest.getState();
        String newState = originalState;

        if (referer != null && !referer.isEmpty()) {
            String encodedReferer = URLEncoder.encode(referer, StandardCharsets.UTF_8);
            newState = originalState + "::" + encodedReferer;
        }

        return OAuth2AuthorizationRequest.from(authorizationRequest)
                .additionalParameters(additionalParameters)
                .state(newState) // 커스텀 state 설정
                .build();
    }
}
