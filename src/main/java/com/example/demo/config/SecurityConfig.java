package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

// TODO: replace permitAll() with JWT filter and role-based rules when JWT is implemented

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    	http
        .csrf(csrf -> csrf.disable())
        .headers(headers -> headers
            .contentTypeOptions(Customizer.withDefaults())
            .frameOptions(frame -> frame.deny())
            .httpStrictTransportSecurity(hsts -> hsts
                .includeSubDomains(true)
                .maxAgeInSeconds(31536000))
            .contentSecurityPolicy(csp -> csp
                .policyDirectives("default-src 'self'"))
        )
        .authorizeHttpRequests(auth -> auth
            .anyRequest().permitAll()
        );
        return http.build();
    }
}