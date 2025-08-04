package com.dontgoback.msa.auth.config.jwt;

import com.dontgoback.msa.auth.config.client.ClientAuthProperties;
import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Date;

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenProvider {
    private final JwtProperties jwtProperties;
    private final ClientAuthProperties clientAuthProperties;
    private final UserDetailsService userDetailsService;
    private final PemKeyLoader pemKeyLoader;

    private final Duration EXPIRE_DURATION = Duration.ofMinutes(5); // 5분

    public String generateToken(String subject) {
        return makeToken(subject);
    }

    public String makeToken(String subject) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + EXPIRE_DURATION.toMillis());
        return Jwts.builder()
                // 발급해주는 주체자 from
                .setIssuer(jwtProperties.getIssuer())
                // 발급받는 대상자 to
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(SignatureAlgorithm.RS256, pemKeyLoader.getPrivateKey())
                .compact();
    }

    public Claims parseAndValidate(String token){
        try {
            Claims claims = getClaims(token);

            if (! issuerMatches(claims)) {
                log.warn("JWT issuer 불일치: {}", claims.getIssuer());
                throw new IllegalArgumentException("잘못된 issuer");
            }
            if (!subjectMatches(claims)) {
                log.warn("JWT subject(clientId) 불일치: {}", claims.getSubject());
                throw new IllegalArgumentException("잘못된 clientId");
            }
            return claims;
        } catch (ExpiredJwtException e) {
            log.warn("만료된 토큰입니다: {}", e.getMessage());
            throw new IllegalArgumentException("만료된 서버 인증 토큰입니다.");
        } catch (JwtException e) {
            log.warn("JWT 서명 검증 실패 또는 파싱 에러", e);
            throw new IllegalArgumentException("유효하지 않은 서버 인증 토큰입니다.");
        } catch (Exception e) {
            log.error("JWT 파싱 중 알 수 없는 오류", e);
            throw new IllegalStateException("JWT 검증 중 예기치 못한 오류가 발생했습니다.");
        }
    }

    public Authentication getAuthentication(Claims claims){
        String clientId = getClientId(claims);
        UserDetails authUser = userDetailsService.loadUserByUsername(clientId);
        return new UsernamePasswordAuthenticationToken(
                // principal : 인증된 사용자 정보
                // Credentals : 무엇으로 인증했는가 기록하는 부분 (보안을 위해 비워둠)
                authUser, "", authUser.getAuthorities()
        );
    }

    private boolean issuerMatches(Claims claims) {
        String issuer = claims.getIssuer();
        String allowedIssuer = jwtProperties.getIssuer();
        return allowedIssuer.equals(issuer);
    }

    private boolean subjectMatches(Claims claims) {
        String clientId = claims.getSubject(); // subject에 clientId를 검증
        return clientAuthProperties.exists(clientId);
    }

    private String getClientId(Claims claims){
        return claims.getSubject();
    }

    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(pemKeyLoader.getPublicKey())
                .build()
                .parseClaimsJws(token)  // 여기서 자동으로 만료시간도 검증함
                .getBody();
    }
}

