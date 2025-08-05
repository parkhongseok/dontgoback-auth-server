package com.dontgoback.msa.auth.config;

import com.dontgoback.msa.auth.config.jwt.TokenProvider;
import com.dontgoback.msa.auth.config.key.PemKeyLoader;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.security.PublicKey;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AuthServerTest {
    @Autowired
    private TestAuthProperties testProps;

    @Autowired
    private TestClientA clientA;

    @Autowired
    private TestTokenVerifier verifier;

    @Autowired
    private TokenProvider provider;

    @Autowired
    private PemKeyLoader pemKeyLoader;

    private String token;


    @BeforeAll
    void setup() throws Exception {
        String clientId = clientA.getId();
        token = provider.makeToken(clientId);
        System.out.println("발급받은 JWT (setup): " + token);
    }

    /* ---------- 공개키 로딩 ---------- */
    @Test
    @DisplayName("공개키가 정상 로딩된다")
    void 공개키_로딩_성공() throws Exception{
        PublicKey publicKey = pemKeyLoader.getPublicKey();
        assertNotNull(publicKey);
        System.out.println("공개키 확인: " + publicKey.getAlgorithm());
    }

    /* ---------- 토큰 발급 ---------- */
    @Test
    @DisplayName("setup 단계에서 발급된 JWT가 null 이 아니다")
    void 발급_토큰_확인() {
        assertNotNull(token);
        System.out.println("저장된 토큰 사용: " + token);
    }

    /* ---------- 토큰 검증 ---------- */
    @Test
    @DisplayName("JWT 검증 후 issuer·subject 가 예상값과 일치한다")
    void 토큰_유효성_검사() {
        Claims claims = verifier.parseAndValidate(token);
        assertEquals(testProps.getJwt().getIssuer(), claims.getIssuer());
        assertTrue(testProps.isClientExists(claims.getSubject()));
    }
}