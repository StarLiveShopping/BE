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


        try{
            // 요청 헤더에서 Bearer 토큰을 추출
            String token = parseBearerToken(request);
            if(token == null) {
                filterChain.doFilter(request, response);
                return;
            }

            // 토큰 유효성 검사 및 socialId 추출
            String socialId = jwtProvider.validateToken(token);
            if(socialId == null) {
                filterChain.doFilter(request, response);
                return;
            }

            // DB에서 사용자 정보 조회
            UserEntity userEntity = userRepository.findBySocialId(socialId).orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
            UserType role = userEntity.getUserType();

            // 사용자의 권한(UserType)을 SecurityContext에 등록하기 위해 변환
            List<GrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority(role.name()));

            // SecurityContext 생성 및 사용자 인증 객체 설정
            SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
            AbstractAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userEntity, null, authorities);
            // 요청 정보 설정 (IP, 세션 ID 등 추가)
            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // SecurityContext에 인증 객체 저장
            securityContext.setAuthentication(authenticationToken);
            SecurityContextHolder.setContext(securityContext);

        }catch(Exception exception) {
            exception.printStackTrace();
        }

        filterChain.doFilter(request, response);
    }


    // 요청 헤더에서 Bearer 토큰을 추출
    private String parseBearerToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");

        boolean hasAuthorization = StringUtils.hasText(authorization);

        if(!hasAuthorization) return null;

        boolean isBearer = authorization.startsWith("Bearer ");
        if(!isBearer) return null;

        String token = authorization.substring(7);
        return token;
    }
}
