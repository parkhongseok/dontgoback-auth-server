package com.dontgoback.msa.auth.config.key;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties("auth.key")
public class KeyProperties {
    private String publicKeyPath;
    private String privateKeyPath;
}
