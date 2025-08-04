package com.dontgoback.msa.auth.config.client;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
@ConfigurationProperties(prefix = "auth")
@Getter
public class ClientAuthProperties {
    private final Map<String, String> clients = new HashMap<>();

    public String getSecretForClientId(String clientId) {
        return clients.get(clientId);  // 간단하게 바로 꺼냄
    }

    public boolean exists(String clientId) {
        return clients.containsKey(clientId);
    }
}