package com.akentech.kbf.expense;

import com.akentech.kbf.expense.model.Expense;
import com.akentech.kbf.expense.repository.ExpenseRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class ExpenseControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:latest");

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @BeforeEach
    void setUp() {
        // Clear the database before each test
        expenseRepository.deleteAll().block();
    }

    @AfterEach
    void tearDown() {
        // Clear the database after each test
        expenseRepository.deleteAll().block();
    }

    @Test
    void testCreateExpense() {
        Expense expense = Expense.builder()
                .reason("Office Supplies")
                .expenseDate(LocalDate.now())
                .qtyPurchased(5)
                .amountPaid(BigDecimal.valueOf(100))
                .expectedAmount(BigDecimal.valueOf(120))
                .receipt("receipt123")
                .createdBy("John Doe")
                .build();

        webTestClient.post()
                .uri("/api/expenses")
                .body(Mono.just(expense), Expense.class)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.reason").isEqualTo("Office Supplies")
                .jsonPath("$.dueBalance").isEqualTo(20.00);
    }

    @Test
    void testCreateExpenseWithInvalidData() {
        // Missing required fields
        Expense invalidExpense = Expense.builder()
                .expenseDate(LocalDate.now())
                .qtyPurchased(5)
                .amountPaid(BigDecimal.valueOf(100))
                .expectedAmount(BigDecimal.valueOf(120))
                .receipt("receipt123")
                .build();

        webTestClient.post()
                .uri("/api/expenses")
                .body(Mono.just(invalidExpense), Expense.class)
                .exchange()
                .expectStatus().isBadRequest();

        // Invalid values (negative amountPaid)
        Expense invalidExpense2 = Expense.builder()
                .reason("Office Supplies")
                .expenseDate(LocalDate.now())
                .qtyPurchased(5)
                .amountPaid(BigDecimal.valueOf(-100)) // Invalid
                .expectedAmount(BigDecimal.valueOf(120))
                .receipt("receipt123")
                .createdBy("John Doe")
                .build();

        webTestClient.post()
                .uri("/api/expenses")
                .body(Mono.just(invalidExpense2), Expense.class)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void testGetExpenseById() {
        Expense expense = Expense.builder()
                .reason("Office Supplies")
                .expenseDate(LocalDate.now())
                .qtyPurchased(5)
                .amountPaid(BigDecimal.valueOf(100))
                .expectedAmount(BigDecimal.valueOf(120))
                .receipt("receipt123")
                .createdBy("John Doe")
                .build();

        expense.calculateDueBalance(); // Ensure dueBalance is set before saving

        Expense savedExpense = expenseRepository.save(expense).block();

        assert savedExpense != null;
        webTestClient.get()
                .uri("/api/expenses/" + savedExpense.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.reason").isEqualTo("Office Supplies")
                .jsonPath("$.dueBalance").exists() // Ensure the field is present
                .jsonPath("$.dueBalance").isEqualTo(20.00); // Verify the correct value
    }

    @Test
    void testGetExpenseByInvalidId() {
        // Invalid ID format (should return 400 BAD_REQUEST)
        webTestClient.get()
                .uri("/api/expenses/invalid-id") // Invalid ObjectId format
                .exchange()
                .expectStatus().isBadRequest();

        // Valid ObjectId format but non-existent (should return 404 NOT_FOUND)
        webTestClient.get()
                .uri("/api/expenses/1234567890abcdef12345678")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void testUpdateExpense() {
        Expense expense = Expense.builder()
                .reason("Office Supplies")
                .expenseDate(LocalDate.now())
                .qtyPurchased(5)
                .amountPaid(BigDecimal.valueOf(100))
                .expectedAmount(BigDecimal.valueOf(120))
                .receipt("receipt123")
                .createdBy("John Doe")
                .build();

        Expense savedExpense = expenseRepository.save(expense).block();

        Expense updatedExpense = Expense.builder()
                .reason("Updated Office Supplies")
                .expenseDate(LocalDate.now())
                .qtyPurchased(5)
                .amountPaid(BigDecimal.valueOf(110))
                .expectedAmount(BigDecimal.valueOf(120))
                .receipt("receipt123")
                .createdBy("John Doe")
                .build();

        assert savedExpense != null;
        webTestClient.put()
                .uri("/api/expenses/" + savedExpense.getId())
                .body(Mono.just(updatedExpense), Expense.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.reason").isEqualTo("Updated Office Supplies")
                .jsonPath("$.dueBalance").isEqualTo(10.00);
    }

    @Test
    void testUpdateExpenseWithInvalidData() {
        // Invalid ID format (should return 400 BAD_REQUEST)
        Expense updatedExpense = Expense.builder()
                .reason("Updated Office Supplies")
                .expenseDate(LocalDate.now())
                .qtyPurchased(5)
                .amountPaid(BigDecimal.valueOf(110))
                .expectedAmount(BigDecimal.valueOf(120))
                .receipt("receipt123")
                .createdBy("John Doe")
                .build();

        webTestClient.put()
                .uri("/api/expenses/invalid-id") // Invalid ObjectId format
                .body(Mono.just(updatedExpense), Expense.class)
                .exchange()
                .expectStatus().isBadRequest();

        // Valid ObjectId format but non-existent (should return 404 NOT_FOUND)
        webTestClient.put()
                .uri("/api/expenses/1234567890abcdef12345678")
                .body(Mono.just(updatedExpense), Expense.class)
                .exchange()
                .expectStatus().isNotFound();

        // Invalid values (negative amountPaid) (should return 400 BAD_REQUEST)
        Expense invalidExpense = Expense.builder()
                .reason("Updated Office Supplies")
                .expenseDate(LocalDate.now())
                .qtyPurchased(5)
                .amountPaid(BigDecimal.valueOf(-110)) // Invalid
                .expectedAmount(BigDecimal.valueOf(120))
                .receipt("receipt123")
                .createdBy("John Doe")
                .build();

        webTestClient.put()
                .uri("/api/expenses/1234567890abcdef12345678")
                .body(Mono.just(invalidExpense), Expense.class)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void testDeleteExpense() {
        Expense expense = Expense.builder()
                .reason("Office Supplies")
                .expenseDate(LocalDate.now())
                .qtyPurchased(5)
                .amountPaid(BigDecimal.valueOf(100))
                .expectedAmount(BigDecimal.valueOf(120))
                .receipt("receipt123")
                .createdBy("John Doe")
                .build();

        Expense savedExpense = expenseRepository.save(expense).block();

        assert savedExpense != null;

        webTestClient.delete()
                .uri("/api/expenses/" + savedExpense.getId())
                .exchange()
                .expectStatus().isNoContent();

        // Verify that the expense was actually deleted
        webTestClient.get()
                .uri("/api/expenses/" + savedExpense.getId())
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void testDeleteExpenseWithInvalidId() {
        // Invalid ID format (should return 400 BAD_REQUEST)
        webTestClient.delete()
                .uri("/api/expenses/invalid-id")
                .exchange()
                .expectStatus().isBadRequest();

        // Non-existent but valid MongoDB ObjectId (should return 404 NOT_FOUND)
        webTestClient.delete()
                .uri("/api/expenses/1234567890abcdef12345678")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void testGetAllExpenses() {
        Expense expense1 = Expense.builder()
                .reason("Office Supplies")
                .expenseDate(LocalDate.now())
                .qtyPurchased(5)
                .amountPaid(BigDecimal.valueOf(100))
                .expectedAmount(BigDecimal.valueOf(120))
                .receipt("receipt123")
                .createdBy("John Doe")
                .build();

        Expense expense2 = Expense.builder()
                .reason("Travel Expenses")
                .expenseDate(LocalDate.now())
                .qtyPurchased(2)
                .amountPaid(BigDecimal.valueOf(200))
                .expectedAmount(BigDecimal.valueOf(250))
                .receipt("receipt456")
                .createdBy("Jane Doe")
                .build();

        expenseRepository.save(expense1).block();
        expenseRepository.save(expense2).block();

        webTestClient.get()
                .uri("/api/expenses")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Expense.class)
                .hasSize(2);
    }

    @Test
    void testGetAllExpensesWithNoData() {
        webTestClient.get()
                .uri("/api/expenses")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Expense.class)
                .hasSize(0); // Verify that no expenses are returned
    }
}