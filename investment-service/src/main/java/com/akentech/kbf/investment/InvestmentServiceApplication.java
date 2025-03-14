package com.akentech.kbf.investment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.akentech.kbf")
public class InvestmentServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(InvestmentServiceApplication.class, args);
    }
}