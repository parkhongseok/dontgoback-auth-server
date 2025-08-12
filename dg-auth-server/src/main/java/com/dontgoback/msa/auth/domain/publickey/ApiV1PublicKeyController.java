package com.dontgoback.msa.auth.domain.publickey;

import com.dontgoback.msa.auth.config.key.PemKeyLoader;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.PublicKey;
import java.util.Base64;

@RestController
@RequestMapping("/msa/auth/api")
@RequiredArgsConstructor
public class ApiV1PublicKeyController {

    private final PemKeyLoader pemKeyLoader;

    @GetMapping("/public-key")
    public ResponseEntity<String> getPublicKey(){
        PublicKey publicKey = pemKeyLoader.getPublicKey();

        if (publicKey == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Public key is not initialized");
        }
        String base64Key = Base64.getEncoder().encodeToString(publicKey.getEncoded());
        return ResponseEntity.ok(base64Key);
    }
}
