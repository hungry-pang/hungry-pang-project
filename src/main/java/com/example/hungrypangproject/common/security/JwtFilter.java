package com.example.hungrypangproject.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j(topic = "JwtFilter")
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 토큰 발급받는 로그인의 경우, 토큰 검사가 없어도 통과
        String requestURL = request.getRequestURI();

        if (requestURL.equals("/api/singup") || requestURL.equals("/api/sinin")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 토큰 유무 검증
        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            log.info("JWT 토큰이 필요합니다.");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "JWT 토큰이 필요 합니다.");
            return;
        }
        // 토큰 있을 때, 토큰 유효성 체크
        String jwt = authorizationHeader.substring(7);

        if (!jwtUtil.validateToken(jwt)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "유효하지 않은 토큰입니다.");
            return;
        }

        // 유효할 경우, 정보 확인
        String email = jwtUtil.extractEmail(jwt);
        request.setAttribute("email", email);

        filterChain.doFilter(request, response);
    }
}
