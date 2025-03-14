package com.akentech.kbf.expense;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.testcontainers.utility.TestcontainersConfiguration;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
public class ExpenseServiceApplicationTests {
    @Test
    void contextLoads() {
    }

}