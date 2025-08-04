package com.dontgoback.msa.auth.config;

import com.dontgoback.msa.auth.config.jwt.TokenAuthenticationFilter;
import com.dontgoback.msa.auth.config.jwt.TokenProvider;
import jakarta.servlet.Filter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
    private final TokenProvider tokenProvider;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
        http
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable) // HTTP Basic 비활성화
                .formLogin(AbstractHttpConfigurer::disable) // Form 로그인 비활성화
                .logout(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS) // 세션 사용 안함
        );

        // 헤더를 확인할 커스텀 필터를 추가 (헤더에서 유저 토큰 뜯어서 유효하다면, 이제 시큐리티 상에서 인증된 유저로 취급)
        http
                .addFilterBefore(tokenAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        http
                .exceptionHandling(ex -> ex.
                authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                );

        http
                .authorizeHttpRequests(auth -> auth
                .requestMatchers("/mas/auth/v1/token").permitAll()
                .requestMatchers("/mas/auth/**").authenticated()
                .anyRequest().permitAll()
        );

        return http.build();
    }

    @Bean
    public Filter tokenAuthenticationFilter() {
        return new TokenAuthenticationFilter(tokenProvider);
    }
}
