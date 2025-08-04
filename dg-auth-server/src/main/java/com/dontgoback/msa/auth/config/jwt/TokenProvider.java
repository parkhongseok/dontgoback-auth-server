package com.dontgoback.msa.auth.config.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class TokenProvider {
    private final JwtProperties jwtProperties;
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

    public boolean isTokenValid(String token) {
        try {
            Jws<Claims> claims = Jwts.parser()
                    .setSigningKey(pemKeyLoader.getPublicKey())
                    .parseClaimsJws(token);
            return !claims.getBody().getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    public Authentication getAuthentication(String token) {
        String username = getSubject(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        return new UsernamePasswordAuthenticationToken(
                // principal : 인증된 사용자 정보
                // 	Credentals : 무엇으로 인증했는가 기록하는 부분(보안을 위해 비워둠)
                userDetails, "", userDetails.getAuthorities()
        );
    }

    // 이 토큰은 누구를 위한 것인가?
    private String getSubject(String token) {
        return Jwts.parser()
                .setSigningKey(pemKeyLoader.getPublicKey())
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}

