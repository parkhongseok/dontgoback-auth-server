package com.dontgoback.msa.auth.config.key;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Getter
@Component
public class PemKeyLoader {
    private final PrivateKey privateKey;
    private final PublicKey publicKey;

    public PemKeyLoader(KeyProperties keyProperties) throws Exception {
        this.privateKey = loadPrivateKey(keyProperties.getPrivateKeyPath());
        this.publicKey = loadPublicKey(keyProperties.getPublicKeyPath());
    }

    private PrivateKey loadPrivateKey(String path) throws Exception {
        try (InputStream inputStream = Files.newInputStream(Paths.get(path))) {
            String key = new String(inputStream.readAllBytes())
                    .replaceAll("-----BEGIN (.*)-----", "")
                    .replaceAll("-----END (.*)-----", "")
                    .replaceAll("\\s", "");
            byte[] decoded = Base64.getDecoder().decode(key);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decoded);
            return KeyFactory.getInstance("RSA").generatePrivate(keySpec);
        }
    }

    private PublicKey loadPublicKey(String path) throws Exception {
        try (InputStream inputStream = Files.newInputStream(Paths.get(path))) {
            String key = new String(inputStream.readAllBytes())
                    .replaceAll("-----BEGIN (.*)-----", "")
                    .replaceAll("-----END (.*)-----", "")
                    .replaceAll("\\s", "");
            byte[] decoded = Base64.getDecoder().decode(key);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decoded);
            return KeyFactory.getInstance("RSA").generatePublic(keySpec);
        }
    }

}
