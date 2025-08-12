package com.teckit.festival.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.access.intercept.AuthorizationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // @PreAuthorize 활성화
public class SecurityConfig {

    private final HeaderAuthenticationFilter headerAuthFilter;

    public SecurityConfig(HeaderAuthenticationFilter headerAuthFilter) {
        this.headerAuthFilter = headerAuthFilter;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/api/festival/manage/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/festival/manage/**").authenticated()
                        .anyRequest().permitAll()
                )
                .addFilterBefore(headerAuthFilter, AuthorizationFilter.class)
                .build();
    }
}
