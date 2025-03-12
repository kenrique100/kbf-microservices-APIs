package com.akentech.shared.models.expense;

import org.springframework.boot.SpringApplication;
import org.testcontainers.utility.TestcontainersConfiguration;

public class TestExpenseServiceApplication {

    public static void main(String[] args) {
        SpringApplication.from(ExpenseServiceApplication::main).with(TestcontainersConfiguration.class).run(args);
    }
}