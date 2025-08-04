package com.dontgoback.msa.auth.domain.key;

import com.dontgoback.msa.auth.config.jwt.PemKeyLoader;
import com.dontgoback.msa.auth.domain.key.dto.PublicKeyResponse;
import com.dontgoback.msa.auth.global.responseDto.ResData;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.PublicKey;
import java.util.Base64;

@RestController
@RequestMapping("/msa/auth")
@RequiredArgsConstructor
public class ApiV1PublicKeyController {

    private final PemKeyLoader pemKeyLoader;

    @GetMapping("/public-key")
    public ResponseEntity<ResData<PublicKeyResponse>> getPublicKey(){
        PublicKey publicKey = pemKeyLoader.getPublicKey();

        if (publicKey == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResData.of("F", "Public key is not initialized", null));
        }

        String base64Key = Base64.getEncoder().encodeToString(publicKey.getEncoded());
        return ResponseEntity.ok(
                ResData.of("S", "Success", new PublicKeyResponse(base64Key))
        );
    }
}
