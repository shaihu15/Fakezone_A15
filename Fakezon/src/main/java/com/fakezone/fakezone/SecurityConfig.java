package com.fakezone.fakezone;

import com.vaadin.flow.spring.security.VaadinWebSecurity;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

@Configuration
public class SecurityConfig extends VaadinWebSecurity {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/VAADIN/**",
                    "/HEARTBEAT/**",
                    "/UIDL/**",
                    "/resources/**",
                    "/webjars/**",
                    "/frontend/**",
                    "/frontend-es6/**",
                    "/frontend-es5/**",
                    "/static/**",
                    "/public/**",
                    "/images/**",
                    "/icons/**",
                    "/manifest.webmanifest",
                    "/sw.js",
                    "/offline.html"
                ).permitAll()
                .anyRequest().permitAll()
            );
    }
}
