package com.example.integration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@EnableJpaRepositories(basePackages = "com.example.integration.repository")
@EntityScan(basePackages = "com.example.integration.entity")
public class WitsApiIntegrationApplication {

    public static void main(String[] args) {
        SpringApplication.run(WitsApiIntegrationApplication.class, args);
    }
}

