package com.example.calculator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.example.calculator.repository")
@EntityScan(basePackages = "com.example.calculator.entity")
public class TariffCalculatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(TariffCalculatorApplication.class, args);
    }
}

