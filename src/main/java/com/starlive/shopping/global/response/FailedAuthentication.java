package com.starlive.shopping.global.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.web.AuthenticationEntryPoint;

public class FailedAuthentication implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
        org.springframework.security.core.AuthenticationException authException)
        throws IOException, ServletException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        // ResponseDto 응답
        response.getWriter().write(new ObjectMapper().writeValueAsString(ApiResponse.error("Fail to authentication.")));
    }
}
