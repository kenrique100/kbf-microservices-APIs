package com.akentech.kbf.expense;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
public class ExpenseServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ExpenseServiceApplication.class, args);
    }
}