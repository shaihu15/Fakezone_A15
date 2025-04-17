package com.fakezone.fakezone.config;

import static org.springframework.security.config.Customizer.withDefaults;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.apache.commons.lang3.ArrayUtils;

@Configuration
public class SecurityConfig {

    private static final String[] PUBLIC_PATHS = {
        "/", "/login"
    };
    private static final String[] STATIC_RESOURCE_PATHS = {
        "/css/**", "/js/**", "/images/**", "/webjars/**"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Configure security filter chain
        return http
            // Configure authorization for HTTP requests
            .authorizeHttpRequests(auth -> auth
            // Allow public access to static resources and login pages
            .requestMatchers(ArrayUtils.addAll(PUBLIC_PATHS, STATIC_RESOURCE_PATHS)).permitAll()
            // Require authentication for all other requests
            .anyRequest().authenticated()
            )
            // Enable OAuth2 login with default settings
            .oauth2Login(withDefaults() 
            )
            // Build and return the security filter chain
            .build();
    }
}