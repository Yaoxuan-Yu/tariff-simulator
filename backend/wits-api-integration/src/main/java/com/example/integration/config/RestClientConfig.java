package com.example.integration.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

// rest client configuration for WITS API calls
@Configuration
public class RestClientConfig {
    
    @Bean
    public RestClient restClient() {
        return RestClient.create();
    }
}

