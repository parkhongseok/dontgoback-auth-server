package com.dontgoback.msa.auth.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "ext-server.client-1")
public class TestClientA {
    private String id;
    private String secret;
}
