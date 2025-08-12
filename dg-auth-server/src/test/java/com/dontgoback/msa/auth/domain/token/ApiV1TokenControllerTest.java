package com.dontgoback.msa.auth.domain.token;

import com.dontgoback.msa.auth.config.client.ClientProperties;
import com.dontgoback.msa.auth.config.jwt.TokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

import java.util.stream.Stream;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ApiV1TokenController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ApiV1TokenControllerTest.TestConfig.class)
@ActiveProfiles("test")
class ApiV1TokenControllerTest {

    private static final String END_POINT = "/msa/auth/api/token";
    private static final String UNAUTHORIZED_MSG =
            "Unauthorized: invalid_client or secret mismatch";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ClientProperties clientProps;

    @Autowired
    private TokenProvider tokenProvider;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public ClientProperties clientProperties() {
            return Mockito.mock(ClientProperties.class);
        }
        @Bean
        public TokenProvider tokenProvider() {
            return Mockito.mock(TokenProvider.class);
        }
    }
    /* ---------- 성공 케이스 ---------- */
    @DisplayName("정상 토큰 발급")
    @ParameterizedTest(name = "[{index}] {0} → 200 OK")
    @MethodSource("successCases")
    void issueToken_success(String clientId, String secret, String expectedToken) throws Exception {

        given(clientProps.getSecretForClientId(clientId)).willReturn(secret);
        given(tokenProvider.generateToken(clientId)).willReturn(expectedToken);

        perform(clientId, secret, status().isOk(), content().string(expectedToken));
    }

    /* ---------- 실패 케이스 ---------- */
    @DisplayName("실패 시나리오 (시크릿 불일치 또는 미등록 clientId)")
    @ParameterizedTest(name = "[{index}] {0} → 401 Unauthorized")
    @MethodSource("failureCases")
    void issueToken_failure(String clientId, String secret) throws Exception {

        // getSecretForClientId 콜백 설정 (null 또는 올바른 시크릿 반환하지만 secret 불일치)
        given(clientProps.getSecretForClientId(clientId))
                .willReturn("dont-care"); // null이어도, 달라도 무관

        perform(clientId, secret, status().isUnauthorized(),
                content().string(UNAUTHORIZED_MSG));
    }

    /* ---------- 데이터 프로바이더 ---------- */
    private static Stream<Arguments> successCases() {
        return Stream.of(
                Arguments.of(
                        "dontgoback-core-server", "core-secret", "mocked-token-A")
        );
    }

    private static Stream<Arguments> failureCases() {
        return Stream.of(
                // 시크릿 불일치
                org.junit.jupiter.params.provider.Arguments.of(
                        "dontgoback-core-server", "wrong-secret"),
                // 미등록 clientId
                org.junit.jupiter.params.provider.Arguments.of(
                        "unknown-client", "any-secret")
        );
    }

    /* ---------- 공통 요청 헬퍼 ---------- */
    private void perform(String clientId, String secret,
                         ResultMatcher status, ResultMatcher content) throws Exception {

        String body = """
                { "clientId": "%s", "clientSecret": "%s" }
                """.formatted(clientId, secret);

        mockMvc.perform(post(END_POINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status)
                .andExpect(content);
    }
}
