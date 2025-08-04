package com.dontgoback.msa.auth.config.jwt;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private final TokenProvider tokenProvider;
    private static final String HEADER_AUTHORIZATION = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";

    @Override
    public void doFilterInternal(HttpServletRequest request,
                         HttpServletResponse response,
                         FilterChain filterChain)
            throws IOException, ServletException {

        try {
        // 헤더에서 JWT 받아오기
        String token = extractToken(request);
        // 토큰 검증 및 내용 반환
        Claims claims = tokenProvider.parseAndValidate(token);
        // 인증 객체 생성 (권한은 없음, clientId만 포함)
        Authentication auth = tokenProvider.getAuthentication(claims);
        // SecurityContext 에 Authentication 객체를 저장
        SecurityContextHolder.getContext().setAuthentication(auth);

        log.debug("서버 간 인증 성공");
        } catch (Exception e) {
            log.error("서버 간 인증 필터 오류", e);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        filterChain.doFilter(request, response);
    }

    // Request의 Header에서 token 값을 가져옵니다. "JWT" : "TOKEN값'
    private String extractToken(HttpServletRequest request) {

        String bearer = request.getHeader(HEADER_AUTHORIZATION);

        if (bearer != null && bearer.startsWith(TOKEN_PREFIX)) {
            return bearer.substring(TOKEN_PREFIX.length());
        }
        return null;
    }
}
