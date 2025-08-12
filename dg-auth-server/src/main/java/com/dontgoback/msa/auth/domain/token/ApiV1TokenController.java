package com.dontgoback.msa.auth.domain.token;

import com.dontgoback.msa.auth.config.client.ClientProperties;
import com.dontgoback.msa.auth.config.jwt.TokenProvider;
import com.dontgoback.msa.auth.domain.token.dto.TokenRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/msa/auth/api")
@RequiredArgsConstructor
public class ApiV1TokenController {
    private final TokenProvider tokenProvider;
    private final ClientProperties clientAuthProperties;

    @PostMapping("/token")
    public ResponseEntity<String> issueToken(@Valid @RequestBody TokenRequest request) {
        String requestedClientId = request.getClientId();
        String requestedSecret = request.getClientSecret();

        String clientSecret = clientAuthProperties.getSecretForClientId(requestedClientId);

        // clientId가 등록되어 있지 않거나, secret 이 일치하지 않는 경우
        if (clientSecret == null || !clientSecret.equals(requestedSecret)) {
            log.warn("Token 요청 실패: clientId={}, 이유=등록되지 않았거나 secret 불일치", requestedClientId);
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Unauthorized: invalid_client or secret mismatch");
        }

        // subject에는 clientId 또는 client 이름 사용
        String token = tokenProvider.generateToken(requestedClientId);
        return ResponseEntity.ok(token);
    }
}
