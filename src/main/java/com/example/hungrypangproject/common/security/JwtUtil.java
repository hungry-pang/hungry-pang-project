package com.example.hungrypangproject.common.security;

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

    // 기본 셋팅
    public static final String BEARER_PREFIX = "Bearer ";
    private static final long TOKEN_TIME = 60 * 60 * 1000L; // token 발급 시간 60분

    private SecretKey key;
    private JwtParser parser;

    @Value("${jwt.secret.key}")
    private String secretKeyString;

    // jwt 라이브러리에서 필요한 내용들 셋팅
    @PostConstruct
    public void init() {
        byte[] bytes = Decoders.BASE64.decode(secretKeyString);
        this.key = Keys.hmacShaKeyFor(bytes);
        this.parser = Jwts.parser()
                .verifyWith(this.key)
                .build();
    }

    // 토큰 발행 (생성)
    public String generateToken(String nickname,String email, String phoneNo, String address) {
        Date now = new Date();
        return BEARER_PREFIX + Jwts.builder()
                .claim("nickname", nickname)
                .claim("email", email)
                .claim("phoneNo", phoneNo)
                .claim("address", address)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + TOKEN_TIME))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    // 토큰 검증
    public boolean validateToken(String token) {
        if (token == null || token.isBlank()) return false;
        try {
            parser.parseSignedClaims(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.error("유효하지 않는 JWT입니다: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("만료된 JWT입니다: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("지원되지 않는 JWT입니다: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT 토큰이 비어있거나 잘못됐습니다.: {}", e.getMessage());
        } catch (Exception e) {
            log.error("JET 검증 중 알 수 없는 오류가 발생했습니다: {}",e);
        }
        return false;
    }

    // 토큰에 있는 내용 꺼내기
    private Claims extractAllClaims(String token) {
        return parser.parseSignedClaims(token).getPayload();
    }

    public String extractEmail(String token) {
        return extractAllClaims(token).get("email", String.class);
    }
}
