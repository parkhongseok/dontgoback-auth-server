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
import org.springframework.test.web.servlet.ResultMatcher;

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

    private String createRequestBody(String clientId, String clientSecret) {
        return """
            {
                "clientId": "%s",
                "clientSecret": "%s"
            }
        """.formatted(clientId, clientSecret);
    }

    private void performTokenRequest(String clientId, String clientSecret, ResultMatcher expectedStatus, ResultMatcher expectedContent) throws Exception {
        mockMvc.perform(post(END_POINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createRequestBody(clientId, clientSecret))
                        .with(csrf()))
                .andExpect(expectedStatus)
                .andExpect(expectedContent);
    }

    @Test
    void 토큰_정상_발급_요청_성공() throws Exception {
        String clientId = "dontgoback-core-server";
        String clientSecret = "test-core-secret";
        String expectedToken = "mocked-jwt-token";

        given(clientAuthProperties.getSecretForClientId(clientId)).willReturn(clientSecret);
        given(tokenProvider.generateToken(clientId)).willReturn(expectedToken);

        performTokenRequest(
                clientId,
                clientSecret,
                status().isOk(),
                content().string(expectedToken)
        );
    }

    @Test
    void 토큰_발급_실패_시크릿_불일치() throws Exception {
        String clientId = "dontgoback-core-server";
        String correctSecret = "test-core-secret";
        String wrongSecret = "wrong-secret";

        given(clientAuthProperties.getSecretForClientId(clientId)).willReturn(correctSecret);

        performTokenRequest(
                clientId,
                wrongSecret,
                status().isUnauthorized(),
                content().string("invalid_client : Client secret does not match.")
        );
    }

    @Test
    void 토큰_발급_실패_존재하지_않는_클라이언트() throws Exception {
        String unknownClientId = "unknown-client";
        String anySecret = "some-secret";

        given(clientAuthProperties.getSecretForClientId(unknownClientId)).willReturn(null);

        performTokenRequest(
                unknownClientId,
                anySecret,
                status().isUnauthorized(),
                content().string("invalid_client : Client secret does not match.")
        );
    }
}
