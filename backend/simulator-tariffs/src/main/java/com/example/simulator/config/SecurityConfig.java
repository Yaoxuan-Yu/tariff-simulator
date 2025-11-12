package com.example.simulator.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

// overall spring security config for the simulator-tariffs service
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // main security config - uses sessions for user-defined tariffs, public access to all endpoints
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // disable CSRF tokens (API Gateway handles security)
                .csrf(csrf -> csrf.disable())
                // use IF_REQUIRED for session-based tariff storage (simulator mode)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/v3/api-docs/**").permitAll()
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**").permitAll()
                        // trust requests from API Gateway (internal service-to-service)
                        .requestMatchers("/api/**").permitAll()
                        .anyRequest().permitAll()
                )
                .httpBasic(basic -> basic.disable())
                .formLogin(form -> form.disable())
                .logout(logout -> logout.disable());
        
        return http.build();
    }
}

