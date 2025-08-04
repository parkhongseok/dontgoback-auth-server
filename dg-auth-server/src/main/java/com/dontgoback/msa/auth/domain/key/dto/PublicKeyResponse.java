package com.dontgoback.msa.auth.domain.key.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PublicKeyResponse {
    private String publicKey;
}
