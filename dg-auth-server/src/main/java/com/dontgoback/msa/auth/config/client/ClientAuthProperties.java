package com.dontgoback.msa.auth.config.client;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "auth")
@Getter
public class ClientAuthProperties {
    private final Map<String, String> clients = new HashMap<>();

    public String getSecretForClientId(String clientId) {
        return clients.get(clientId);  // 간단하게 바로 꺼냄
    }

    @jakarta.annotation.PostConstruct
    public void debugClients() {
        System.out.println("==== ClientAuthProperties 디버깅 시작 ====");
        if (clients.isEmpty()) {
            System.out.println("클라이언트 설정이 로딩되지 않았습니다.");
        } else {
            clients.forEach((clientId, secret) -> {
                System.out.printf("clientId = %s | secret = %s\n", clientId, secret);
            });
        }
        System.out.println("==== ClientAuthProperties 디버깅 종료 ====");
    }
}