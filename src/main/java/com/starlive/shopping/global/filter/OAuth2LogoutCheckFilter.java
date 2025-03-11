package com.starlive.shopping.global.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.starlive.shopping.global.response.ApiResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

public class OAuth2LogoutCheckFilter extends OncePerRequestFilter {

    private static final String LOGOUT_URL = "/oauth2/logout";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {

        // 요청이 로그아웃 URL인지 확인
        if (LOGOUT_URL.equals(request.getRequestURI())) {

            // 쿠키 검사 (JSESSIONID 또는 refreshToken이 있는지 확인)
            boolean hasSessionCookie = Optional.ofNullable(request.getCookies())
                .map(Arrays::asList)
                .orElseGet(Arrays::asList)
                .stream()
                .anyMatch(cookie -> "JSESSIONID".equals(cookie.getName()) || "refreshToken".equals(cookie.getName()));

            // 쿠키가 없으면 이미 로그아웃된 상태이므로 즉시 응답 반환
            if (!hasSessionCookie) {
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(new ObjectMapper().writeValueAsString(ApiResponse.success("Already logged out.")));
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
