package com.example.transaction.management;

import com.example.transaction.management.model.Transaction;
import com.example.transaction.management.model.TransactionType;
import com.example.transaction.management.repository.SkipListTransactionRepository;
import com.example.transaction.management.repository.TransactionRepository;
import com.example.transaction.management.service.TransactionService;
import com.example.transaction.management.exception.ApiException;
import com.example.transaction.management.exception.TransactionErrorType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

public class TransactionServiceTest {
    private TransactionService service;
    private TransactionRepository repository;
    
    // Maximum page size for pagination validation
    private static final int MAX_PAGE_SIZE = 50;
    
    // Invalid page size for pagination boundary testing
    private static final int INVALID_PAGE_SIZE = MAX_PAGE_SIZE + 1;

    @BeforeEach
    void setUp() {
        repository = new SkipListTransactionRepository();
        service = new TransactionService(repository);
    }

    @Test
    @DisplayName("Should successfully create a new transaction with valid input data")
    void testCreateTransaction() {
        Transaction transaction = new Transaction();
        transaction.setAmount(new BigDecimal("100.00"));
        transaction.setType(TransactionType.DEPOSIT);
        transaction.setDescription("Test deposit transaction");
        transaction.setCategory("Test Category");

        Transaction saved = service.create(transaction);
        assertNotNull(saved.getId());
        assertEquals(new BigDecimal("100.00"), saved.getAmount());
    }

    @Test
    @DisplayName("Should successfully retrieve a transaction by its unique identifier")
    void testGetTransaction() {
        Transaction transaction = new Transaction();
        transaction.setAmount(new BigDecimal("100.00"));
        transaction.setType(TransactionType.DEPOSIT);
        transaction.setDescription("Test deposit transaction");
        transaction.setCategory("Test Category");

        Transaction saved = service.create(transaction);
        Optional<Transaction> retrieved = service.read(saved.getId());
        assertTrue(retrieved.isPresent());
        assertEquals(saved.getId(), retrieved.get().getId());
    }

    @Test
    @DisplayName("Should successfully retrieve paginated list of all transactions")
    void testGetAllTransactions() {
        Transaction transaction1 = new Transaction();
        transaction1.setAmount(new BigDecimal("100.00"));
        transaction1.setType(TransactionType.DEPOSIT);
        transaction1.setDescription("Test deposit transaction 1");
        transaction1.setCategory("Test Category");

        Transaction transaction2 = new Transaction();
        transaction2.setAmount(new BigDecimal("200.00"));
        transaction2.setType(TransactionType.WITHDRAWAL);
        transaction2.setDescription("Test withdrawal transaction");
        transaction2.setCategory("Test Category");

        service.create(transaction1);
        service.create(transaction2);

        List<Transaction> transactions = service.list(0, 10);
        assertEquals(2, transactions.size());
    }

    @Test
    @DisplayName("Should successfully update an existing transaction information")
    void testUpdateTransaction() {
        Transaction transaction = new Transaction();
        transaction.setAmount(new BigDecimal("100.00"));
        transaction.setType(TransactionType.DEPOSIT);
        transaction.setDescription("Test deposit transaction");
        transaction.setCategory("Test Category");

        Transaction saved = service.create(transaction);
        saved.setAmount(new BigDecimal("150.00"));
        Transaction updated = service.update(saved.getId(), saved);
        assertEquals(new BigDecimal("150.00"), updated.getAmount());
    }

    @Test
    @DisplayName("Should successfully delete a transaction by its unique identifier")
    void testDeleteTransaction() {
        Transaction transaction = new Transaction();
        transaction.setAmount(new BigDecimal("100.00"));
        transaction.setType(TransactionType.DEPOSIT);
        transaction.setDescription("Test deposit transaction");
        transaction.setCategory("Test Category");

        Transaction saved = service.create(transaction);
        service.delete(saved.getId());
        Optional<Transaction> retrieved = service.read(saved.getId());
        assertFalse(retrieved.isPresent());
    }

    @Test
    @DisplayName("Should throw exception when pagination parameters are invalid")
    void testInvalidPageSize() {
        // Test with page size exceeding maximum allowed value
        ApiException exception = assertThrows(
            ApiException.class,
            () -> service.list(0, INVALID_PAGE_SIZE)
        );
        assertEquals(TransactionErrorType.INVALID_PAGINATION, exception.getErrorType());

        // Test with negative page size value
        exception = assertThrows(
            ApiException.class,
            () -> service.list(0, -1)
        );
        assertEquals(TransactionErrorType.INVALID_PAGINATION, exception.getErrorType());

        // Test with zero page size value
        exception = assertThrows(
            ApiException.class,
            () -> service.list(0, 0)
        );
        assertEquals(TransactionErrorType.INVALID_PAGINATION, exception.getErrorType());
    }
} 