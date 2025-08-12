package com.dontgoback.msa.auth.config;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "auth")
public class TestAuthProperties {
    private Map<String, String> clients = new HashMap<>();
    private Key key = new Key();
    private Jwt jwt = new Jwt();

    public boolean isClientExists(String clientId) {
        return clients.containsKey(clientId);
    }

    public String getSecretForClientId(String clientId) {
        return clients.get(clientId);  // 간단하게 바로 꺼냄
    }

    @Getter
    @Setter
    public static class Key {
        private String publicKeyPath;
        private String privateKeyPath;
    }

    @Getter
    @Setter
    public static class Jwt {
        private String issuer;
    }
}
