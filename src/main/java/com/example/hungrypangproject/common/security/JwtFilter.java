package com.example.hungrypangproject.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j(topic = "JwtFilter")
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        filterChain.doFilter(request, response); // 임시로 전체 통과
    }

//    @Override
//    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
//            throws ServletException, IOException {
//
//        // 토큰 발급받는 로그인의 경우, 토큰 검사가 없어도 통과
//        String requestURL = request.getRequestURI();
//
//        if (requestURL.equals("/api/member/signup") || requestURL.equals("/api/member/signin")) {
//            filterChain.doFilter(request, response);
//            return;
//        }
//
//        // JWT 토큰 유무 검증
//        String authorizationHeader = request.getHeader("Authorization");
//
//        if (authorizationHeader == null || !authorizationHeader.startsWith(JwtUtil.BEARER_PREFIX)) {
//            log.warn("JWT 토큰이 없거나 형식이 잘못되었습니다. URL: {}", requestURL);
//            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//            response.setCharacterEncoding("UTF-8");
//            response.getWriter().write("{\"message\": \"인증이 필요합니다.\"}");
//            return;
//        }
//
//        // Access Token 추출
//        String jwt = jwtUtil.substringToken(authorizationHeader);
//
//        // Refresh token 인증 아님
//        if (!jwtUtil.validateToken(jwt)) {
//            // 만료되었다면 401 에러 후 Refresh Token 사용 유도
//            log.warn("JWT 토큰이 만료되었거나 유효하지 않습니다. URL: {}", requestURL);
//            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//            response.setCharacterEncoding("UTF-8");
//            response.getWriter().write("{\"message\": \"토큰이 만료되었거나 유효하지 않습니다.\"}");
//            return;
//        }
//        // 토큰 있을 때, 토큰 유효성 체크
//        try {
//            String email = jwtUtil.extractEmail(jwt);
//            UserDetails userDetails = userDetailsService.loadUserByUsername(email);
//
//            UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
//                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
//
//            // SecurityContext에 인증 정보 넣기
//            SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
//
//        } catch (Exception e) {
//            log.error("사용자 인증 설정 중 에러 발생: {}", e.getMessage());
//            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
//            return;
//        }
//
//        // 필터 이동
//        filterChain.doFilter(request, response);
//    }
}