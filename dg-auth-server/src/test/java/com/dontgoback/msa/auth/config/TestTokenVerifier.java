package com.dontgoback.msa.auth.config;

import com.dontgoback.msa.auth.config.key.PemKeyLoader;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class TestTokenVerifier {

    @Autowired
    private TestAuthProperties testProps;

    @Autowired
    private PemKeyLoader pemKeyLoader;

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

    private boolean issuerMatches(Claims claims) {
        String issuer = claims.getIssuer();
        String allowedIssuer = testProps.getJwt().getIssuer();
        return allowedIssuer.equals(issuer);
    }

    private boolean subjectMatches(Claims claims) {
        String clientId = claims.getSubject(); // subject에 clientId를 검증
        return testProps.isClientExists(clientId);
    }

    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(pemKeyLoader.getPublicKey())
                .build()
                .parseClaimsJws(token)  // 여기서 자동으로 만료시간도 검증함
                .getBody();
    }
}
