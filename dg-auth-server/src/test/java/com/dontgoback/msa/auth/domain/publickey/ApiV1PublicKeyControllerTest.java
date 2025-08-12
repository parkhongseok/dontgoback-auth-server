package com.dontgoback.msa.auth.domain.publickey;

import com.dontgoback.msa.auth.config.key.PemKeyLoader;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.util.Base64;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(ApiV1PublicKeyController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ApiV1PublicKeyControllerTest.TestConfig.class)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ApiV1PublicKeyControllerTest {

    private static final String END_POINT = "/msa/auth/api/public-key";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PemKeyLoader pemKeyLoader;

    private PublicKey mockPublicKey;
    private String encodedKey;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public PemKeyLoader pemKeyLoader() { return Mockito.mock(PemKeyLoader.class); }
    }

    @BeforeAll
    void initKeys() throws Exception {
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(2048);
        KeyPair pair = gen.generateKeyPair();
        mockPublicKey = pair.getPublic();
        encodedKey = Base64.getEncoder().encodeToString(mockPublicKey.getEncoded());
    }

    /* ---------- 성공 케이스 ---------- */
    @Test
    @DisplayName("공개키 요청 시 200 OK 와 Base64 값 반환")
    void 공개키_정상_응답() throws Exception {
        // given
        given(pemKeyLoader.getPublicKey()).willReturn(mockPublicKey);

        // when & then
        mockMvc.perform(get(END_POINT))
                .andExpect(status().isOk())
                .andExpect(content().string(encodedKey));
    }

    /* ---------- 실패 케이스 ---------- */
    @Test
    @DisplayName("공개키 미초기화 시 500 INTERNAL_SERVER_ERROR 반환")
    void 공개키_미초기화_응답() throws Exception {
        // given
        given(pemKeyLoader.getPublicKey()).willReturn(null);

        // when & then
        mockMvc.perform(get(END_POINT))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Public key is not initialized"));
    }
}
