package com.teckit.festival.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable()  // CSRF 비활성화 (JWT 인증 시 주로 사용)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**", "/webjars/**").permitAll()  // Swagger 문서 허용
                        .anyRequest().authenticated()  // 나머지 요청은 인증 필요
                )
                .formLogin().disable()  // Form Login 비활성화
                .httpBasic();  // Basic 인증 사용 (필요시 JWT로 대체 가능)

        return http.build();
    }
}
