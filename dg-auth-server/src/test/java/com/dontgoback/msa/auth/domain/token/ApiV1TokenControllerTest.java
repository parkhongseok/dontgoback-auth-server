package com.dontgoback.msa.auth.domain.token;

import com.dontgoback.msa.auth.config.client.ClientAuthProperties;
import com.dontgoback.msa.auth.config.jwt.TokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false) // Spring Security 필터 제거
@WebMvcTest(ApiV1TokenController.class)
public class ApiV1TokenControllerTest {
    private final String END_POINT =  "/msa/auth/api/token";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TokenProvider tokenProvider;

    @MockitoBean
    private ClientAuthProperties clientAuthProperties;

    @Test
    void 토큰_정상_발급_요청_성공() throws Exception {
        // given
        String clientId = "dontgoback-core-server";
        String clientSecret = "test-core-secret";
        String expectedToken = "mocked-jwt-token";

        given(clientAuthProperties.getSecretForClientId(clientId)).willReturn(clientSecret);
        given(tokenProvider.generateToken(clientId)).willReturn(expectedToken);

        String requestBody = """
            {
                "clientId": "%s",
                "clientSecret": "%s"
            }
        """.formatted(clientId, clientSecret);

        // when & then
        mockMvc.perform(post(END_POINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("S"))
                .andExpect(jsonPath("$.data.token").value(expectedToken));
    }


    @Test
    void 토큰_발급_실패_시크릿_불일치() throws Exception {
        // given
        String clientId = "dontgoback-core-server";
        String correctSecret = "test-core-secret";
        String wrongSecret = "wrong-secret";

        given(clientAuthProperties.getSecretForClientId(clientId)).willReturn(correctSecret);

        String requestBody = """
        {
            "clientId": "%s",
            "clientSecret": "%s"
        }
    """.formatted(clientId, wrongSecret);

        // when & then
        mockMvc.perform(post(END_POINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.resultCode").value("F"))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void 토큰_발급_실패_존재하지_않는_클라이언트() throws Exception {
        // given
        String nonExistentClientId = "unknown-client";
        String anySecret = "some-secret";

        given(clientAuthProperties.getSecretForClientId(nonExistentClientId)).willReturn(null); // 등록되지 않음

        String requestBody = """
        {
            "clientId": "%s",
            "clientSecret": "%s"
        }
    """.formatted(nonExistentClientId, anySecret);

        // when & then
        mockMvc.perform(post(END_POINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.resultCode").value("F"))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

}
