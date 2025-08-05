package com.dontgoback.msa.auth.domain.testclients;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
public class ClientA {
    private final String id = "dontgoback-server-1";
    private final String secret = "test-secret-1";
}
