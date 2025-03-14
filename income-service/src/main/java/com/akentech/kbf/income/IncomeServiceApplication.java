package com.akentech.kbf.income;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.akentech.kbf")public class IncomeServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(IncomeServiceApplication.class, args);
    }
}