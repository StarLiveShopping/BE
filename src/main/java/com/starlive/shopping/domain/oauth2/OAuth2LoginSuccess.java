package com.starlive.shopping.domain.oauth2;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.starlive.shopping.global.response.ApiResponse;
import com.starlive.shopping.global.jwt.JwtProvider;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccess extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtProvider jwtProvider;

    @Value("${cookie.expiration}")
    private int COOKIE_EXPIRATION;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();

        String socialId = oAuth2User.getName();
        String accessToken = jwtProvider.createAccessToken(socialId);
        String refreshToken = jwtProvider.createRefreshToken();

        // Refresh Token 쿠키 설정
        Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);

        refreshTokenCookie.setHttpOnly(true); // http only
        refreshTokenCookie.setSecure(false); // 현재 http -> https로 변경 필요
        refreshTokenCookie.setPath("/"); // 모든 경로에서 사용 가능
        refreshTokenCookie.setMaxAge(COOKIE_EXPIRATION); // 7일 동안 유지

        response.addCookie(refreshTokenCookie);

        // ResponseDto 활용하여 JSON 응답
        ApiResponse<Map<String, Object>> successResponse = ApiResponse.success("Success to login.", Map.of(
            "accessToken", accessToken,
            "tokenType", "Bearer",
            "refreshToken", refreshToken,
            "location", "Cookie"
        ));

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(new ObjectMapper().writeValueAsString(successResponse));
    }
}
