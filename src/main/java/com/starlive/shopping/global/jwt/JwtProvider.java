package com.starlive.shopping.global.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.security.Key;
import javax.crypto.SecretKey;
// import io.jsonwebtoken.Jwts;
// import io.jsonwebtoken.SignatureAlgorithm;
// import io.jsonwebtoken.security.Keys;
// import java.nio.charset.StandardCharsets;
// import java.util.Date;
// import java.security.Key;
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


    public String createAccessToken(String socialId) {
        Date expiredDate = new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION);
        Key key = getSigningKey();
        // Key key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));

        String jwt = Jwts.builder()
            .signWith(key)
            .subject(socialId)
            .issuedAt(new Date())
            .expiration(expiredDate)
            .compact();

        return jwt;

    // Access Token 생성 - jwt 11.5 ver
//     public String createAccessToken(String socialId) {
//         Date expiredDate = new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION);
//         return Jwts.builder()
//             .signWith(getSigningKey(), SignatureAlgorithm.HS256)
//             .setSubject(socialId)
//             .setIssuedAt(new Date())
//             .setExpiration(expiredDate)
//             .compact();
    }

    // Refresh Token 생성
    public String createRefreshToken() {
        Date expiredDate = new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION);
        Key key = getSigningKey();

        String jwt_refresh = Jwts.builder()
            .signWith(key)
            .issuedAt(new Date())
            .expiration(expiredDate)
            .compact();

        return jwt_refresh;
    }

    public String validateToken (String jwt) {
        String subject = null;
        SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));

        // ✅ JWT 검증
        try {
            Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(jwt)
                .getPayload();

            subject = claims.getSubject();
            return subject;
        } catch(Exception exception) {
// jwt - 11.5 ver
//         return Jwts.builder()
//             .signWith(getSigningKey(), SignatureAlgorithm.HS256)
//             .setIssuedAt(new Date())
//             .setExpiration(expiredDate)
//             .compact();
//     }

//     // ✅ JWT 검증
//     public String validateToken(String jwt) {
//         try {
//             return Jwts.parserBuilder()
//                 .setSigningKey(getSigningKey())
//                 .build()
//                 .parseClaimsJws(jwt)
//                 .getBody()
//                 .getSubject();
//         } catch (Exception exception) {
            exception.printStackTrace();
            return null;
        }
    }
}
