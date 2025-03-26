package com.akentech.kbf.income;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class IncomeServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(IncomeServiceApplication.class, args);
        log.info("Income Service started successfully");
    }
}