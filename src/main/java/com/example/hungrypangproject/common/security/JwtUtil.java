package com.example.hungrypangproject.common.security;

import com.example.hungrypangproject.domain.member.entity.MemberRoleEnum;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Slf4j(topic = "JwtUtil")
@Component
public class JwtUtil {

    public static final String BEARER_PREFIX = "Bearer ";
    private static final long ACCESS_TOKEN_TIME = 60 * 1000L; // token 발급 시간 60분
    private static final long REFRESH_TOKEN_TIME = 3 * 60 * 1000L; // Refresh token 발급 2주

    private SecretKey secretKey;
    private JwtParser jwtparser;

    @Value("${jwt.secret.key}")
    private String secretKeyString;

    @PostConstruct
    public void init() {
        byte[] bytes = Decoders.BASE64.decode(secretKeyString);
        this.secretKey = Keys.hmacShaKeyFor(bytes);
        this.jwtparser = Jwts.parser()
                .verifyWith(this.secretKey)
                .build();
    }

    public String createAccessToken(String email, MemberRoleEnum role) {
        Date now = new Date();
        return BEARER_PREFIX + Jwts.builder()
                .subject(email)
                .claim("role", role.name())
                .claim("type", "ACCESS")
                .issuedAt(now)
                .expiration(new Date(now.getTime() + ACCESS_TOKEN_TIME))
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    public String createRefreshToken(String email) {
        Date now = new Date();
        return BEARER_PREFIX + Jwts.builder()
                .subject(email)
                .claim("type", "REFRESH")
                .issuedAt(now)
                .expiration(new Date(now.getTime() + REFRESH_TOKEN_TIME))
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    // 토큰에서 Bearer 없앰
    public String substringToken (String tokenValue) {
        if (tokenValue != null && tokenValue.startsWith(BEARER_PREFIX)) {
        return tokenValue.substring(7);
        }
        throw new NullPointerException("토큰이 없거나 유효하지 않습니다.");
    }

    public boolean validateToken(String token) {
        if (token == null || token.isBlank()) return false;
        try {
            jwtparser.parseSignedClaims(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.error("유효하지 않는 JWT입니다: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("만료된 JWT입니다: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("지원되지 않는 JWT입니다: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT 토큰이 비어있거나 잘못됐습니다.: {}", e.getMessage());
        }
        return false;
    }

    // Redis 저장 RefreshToken 생명주기 연장
    public long getRefreshExpiration() {
        return REFRESH_TOKEN_TIME;
    }

    // 블랙리스트 RefreshToken 유효시간 계산
    public long getExpiration(String token) {
        try {
            Date expiration = getClaims(token).getExpiration();
            long now = new Date().getTime();
            return (expiration.getTime() - now);
        } catch (ExpiredJwtException e) {
            return 0;
        }
    }

    private Claims getClaims (String token) {
        return jwtparser.parseSignedClaims(token).getPayload();
    }

    public String extractEmail(String token) {
        return getClaims(token).getSubject();
    }

    public boolean isRefreshToken(String token) {
        return "REFRESH".equals(getClaims(token).get("type", String.class));
    }
}
