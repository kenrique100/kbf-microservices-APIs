package com.akentech.kbf.investment;

import org.springframework.boot.SpringApplication;
import org.testcontainers.utility.TestcontainersConfiguration;

public class TestInvestmentServiceApplication {

    public static void main(String[] args) {
        SpringApplication.from(InvestmentServiceApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}