package com.dontgoback.msa.auth.domain.token.dto;


import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
public class TokenRequest {
    // Spring이 자동으로 역직렬화(Deserialize)할 수 있어야 하는데, 이 때, 기본 생성자가 필요함
    // Spring은 JSON → Java 객체로 변환할 때 기본 생성자 + setter or field 접근 방식으로 객체를 만듭 이 떄 기본 생성자 필요.
    @NotBlank(message = "clientId는 필수입니다.")
    private String clientId;

    @NotBlank(message = "clientSecret은 필수입니다.")
    private String clientSecret;
}

// TokenRequest는 스프링이 직접 생성하는 객체이므로 기본 생성자가 필요하고,
// TokenResponse는 우리가 직접 생성해서 반환하는 객체이므로 전체 필드를 받는 생성자가 필요함