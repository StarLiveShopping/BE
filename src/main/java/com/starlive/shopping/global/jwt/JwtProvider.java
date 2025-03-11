package com.starlive.shopping.global.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.security.Key;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtProvider {

    @Value("${jwt.secret-key}")
    private String secretKey;

    @Value("${jwt.access-token-expiration}")
    private long ACCESS_TOKEN_EXPIRATION; // 1시간

    @Value("${jwt.refresh-token-expiration}")
    private long REFRESH_TOKEN_EXPIRATION; // 7일

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    // Access Token 생성
    public String createAccessToken(String socialId) {
        Date expiredDate = new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION);
        return Jwts.builder()
            .signWith(getSigningKey(), SignatureAlgorithm.HS256)
            .setSubject(socialId)
            .setIssuedAt(new Date())
            .setExpiration(expiredDate)
            .compact();
    }

    // Refresh Token 생성
    public String createRefreshToken() {
        Date expiredDate = new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION);
        return Jwts.builder()
            .signWith(getSigningKey(), SignatureAlgorithm.HS256)
            .setIssuedAt(new Date())
            .setExpiration(expiredDate)
            .compact();
    }

    // ✅ JWT 검증
    public String validateToken(String jwt) {
        try {
            return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(jwt)
                .getBody()
                .getSubject();
        } catch (Exception exception) {
            exception.printStackTrace();
            return null;
        }
    }
}
