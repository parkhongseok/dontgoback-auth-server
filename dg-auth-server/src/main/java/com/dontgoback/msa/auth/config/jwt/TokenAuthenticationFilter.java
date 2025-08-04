package com.dontgoback.msa.auth.config.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

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


        // 헤더에서 JWT 받아오기
        String token = resolveToken(request);

        // 유효한 토큰인지 확인
        if (token != null && tokenProvider.isTokenValid(token)) {
            // 토큰이 유효하면 토큰으로부터 인증 객체 받아옴
            Authentication auth = tokenProvider.getAuthentication(token);
            // SecurityContext 에 Authentication 객체를 저장
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
        filterChain.doFilter(request, response);
    }

    // Request의 Header에서 token 값을 가져옵니다. "JWT" : "TOKEN값'
    private String resolveToken(HttpServletRequest request) {

        String bearer = request.getHeader(HEADER_AUTHORIZATION);

        if (bearer != null && bearer.startsWith(TOKEN_PREFIX)) {
            return bearer.substring(TOKEN_PREFIX.length());
        }
        return null;
    }
}
