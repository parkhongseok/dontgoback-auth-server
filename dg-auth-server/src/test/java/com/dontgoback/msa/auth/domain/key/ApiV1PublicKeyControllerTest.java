package com.dontgoback.msa.auth.domain.key;

import com.dontgoback.msa.auth.config.jwt.PemKeyLoader;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.util.Base64;

import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.mockito.BDDMockito.given;

@WebMvcTest(ApiV1PublicKeyController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
public class ApiV1PublicKeyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PemKeyLoader pemKeyLoader;

    @Test
    void 공개키_정상_응답() throws Exception {
        // given
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        PublicKey mockPublicKey = keyGen.generateKeyPair().getPublic();
        String encodedKey = Base64.getEncoder().encodeToString(mockPublicKey.getEncoded());

        given(pemKeyLoader.getPublicKey()).willReturn(mockPublicKey);

        // when & then
        mockMvc.perform(get("/msa/auth/public-key"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("S"))
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data.publicKey").value(encodedKey));
    }

    @Test
    void 공개키_미초기화_응답() throws Exception {
        // given
        given(pemKeyLoader.getPublicKey()).willReturn(null);

        // when & then
        mockMvc.perform(get("/msa/auth/public-key"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.resultCode").value("F"))
                .andExpect(jsonPath("$.message").value("Public key is not initialized"))
                .andExpect(jsonPath("$.data").doesNotExist());
    }
}
