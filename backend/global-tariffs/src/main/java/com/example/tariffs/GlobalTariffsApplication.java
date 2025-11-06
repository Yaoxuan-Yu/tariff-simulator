package com.example.tariffs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@EnableJpaRepositories(basePackages = "com.example.tariffs.repository")
@EntityScan(basePackages = "com.example.tariffs.entity")
public class GlobalTariffsApplication {

    public static void main(String[] args) {
        SpringApplication.run(GlobalTariffsApplication.class, args);
    }
}

