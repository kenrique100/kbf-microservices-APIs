package com.akentech.kbf.income;

import org.springframework.boot.SpringApplication;
import org.testcontainers.utility.TestcontainersConfiguration;

public class TestIncomeServiceApplication {

    public static void main(String[] args) {
        SpringApplication.from(IncomeServiceApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}