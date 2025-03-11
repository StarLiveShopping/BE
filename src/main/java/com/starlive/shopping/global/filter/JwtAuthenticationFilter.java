package com.starlive.shopping.global.filter;

import com.starlive.shopping.global.jwt.JwtProvider;
import com.starlive.shopping.domain.user.UserEntity;
import com.starlive.shopping.domain.user.UserEntity.UserType;
import com.starlive.shopping.domain.user.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    // jwt 인증 처리 필터
    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException {

        try {
            // 헤더에서 토큰 추출
            String token = parseBearerToken(request);
            if (token == null) {
                filterChain.doFilter(request, response);
                return;
            }

            // 토큰 유효성 검사
            String socialId = jwtProvider.validateToken(token);
            if (socialId == null) {
                filterChain.doFilter(request, response);
                return;
            }

            // socialId 조회
            UserEntity userEntity = userRepository.findBySocialId(socialId).orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));  // socialId 조회
            UserType role = userEntity.getUserType(); // ADMIN, CUSTOMER, SELLER

            List<GrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority(role.name()));

            SecurityContext securityContext = SecurityContextHolder.getContext();

            // socialId와 userType을 기반으로 인증 토큰 생성
            AbstractAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(socialId, null, authorities);
            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            securityContext.setAuthentication(authenticationToken); // 인증 정보 설정
            SecurityContextHolder.setContext(securityContext);  // 설정된 보안 컨텍스트 스레드에 저장

        } catch(Exception exception) {
            exception.printStackTrace();
        }

        filterChain.doFilter(request, response);
    }

    private String parseBearerToken(HttpServletRequest request) {
        // 헤더에서 jwt 토큰 추출
        String authorization = request.getHeader("Authorization");

        boolean hasAuthorization = StringUtils.hasText(authorization);
        if (!hasAuthorization) { return null; }

        boolean isBearer = authorization.startsWith("Bearer ");
        if (!isBearer) { return null; }

        String token = authorization.substring(7);
        return token;
    }
}
