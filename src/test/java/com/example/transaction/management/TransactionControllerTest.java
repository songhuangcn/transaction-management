package com.example.transaction.management;

import com.example.transaction.management.model.Transaction;
import com.example.transaction.management.model.TransactionType;
import com.example.transaction.management.service.TransactionService;
import com.example.transaction.management.exception.ApiException;
import com.example.transaction.management.controller.TransactionController;
import com.example.transaction.management.exception.TransactionErrorType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionController.class)
public class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionService transactionService;

    @Autowired
    private ObjectMapper objectMapper;

    private Transaction testTransaction;
    private UUID testId;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        testTransaction = new Transaction();
        testTransaction.setId(testId);
        testTransaction.setAmount(new BigDecimal("100.00"));
        testTransaction.setType(TransactionType.DEPOSIT);
        testTransaction.setDescription("Test transaction description");
        testTransaction.setCategory("Test Category");
        testTransaction.setTimestamp(Instant.now());
    }

    @Test
    @DisplayName("Should successfully create a new transaction record")
    void testCreateTransaction() throws Exception {
        when(transactionService.create(any(Transaction.class))).thenReturn(testTransaction);

        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testTransaction)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testId.toString()))
                .andExpect(jsonPath("$.amount").value(100.00))
                .andExpect(jsonPath("$.type").value("DEPOSIT"))
                .andExpect(jsonPath("$.description").value("Test transaction description"))
                .andExpect(jsonPath("$.category").value("Test Category"));
    }

    @Test
    @DisplayName("Should return bad request when creating transaction with invalid data")
    void testCreateTransactionInvalidData() throws Exception {
        Transaction invalidTransaction = new Transaction();
        // Missing required fields for validation testing
        // Bean Validation will handle this automatically

        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidTransaction)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors").exists());
    }

    @Test
    @DisplayName("Should successfully retrieve transaction by unique identifier")
    void testGetTransaction() throws Exception {
        when(transactionService.read(testId)).thenReturn(Optional.of(testTransaction));

        mockMvc.perform(get("/api/transactions/" + testId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testId.toString()))
                .andExpect(jsonPath("$.amount").value(100.00))
                .andExpect(jsonPath("$.type").value("DEPOSIT"))
                .andExpect(jsonPath("$.description").value("Test transaction description"))
                .andExpect(jsonPath("$.category").value("Test Category"));
    }

    @Test
    @DisplayName("Should return not found when transaction identifier does not exist")
    void testGetTransactionNotFound() throws Exception {
        when(transactionService.read(testId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/transactions/" + testId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should successfully retrieve paginated list of transactions")
    void testGetAllTransactions() throws Exception {
        List<Transaction> transactions = Arrays.asList(testTransaction);
        when(transactionService.list(0, 10)).thenReturn(transactions);

        mockMvc.perform(get("/api/transactions")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(testId.toString()))
                .andExpect(jsonPath("$[0].amount").value(100.00))
                .andExpect(jsonPath("$[0].type").value("DEPOSIT"))
                .andExpect(jsonPath("$[0].description").value("Test transaction description"))
                .andExpect(jsonPath("$[0].category").value("Test Category"));
    }

    @Test
    @DisplayName("Should return bad request when pagination parameters are invalid")
    void testGetAllTransactionsInvalidPagination() throws Exception {
        when(transactionService.list(-1, 10))
                .thenThrow(new ApiException(TransactionErrorType.INVALID_PAGINATION));

        mockMvc.perform(get("/api/transactions")
                .param("page", "-1")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should successfully update existing transaction information")
    void testUpdateTransaction() throws Exception {
        Transaction updatedTransaction = new Transaction();
        updatedTransaction.setId(testId);
        updatedTransaction.setAmount(new BigDecimal("200.00"));
        updatedTransaction.setType(TransactionType.WITHDRAWAL);
        updatedTransaction.setDescription("Updated transaction description");
        updatedTransaction.setCategory("Food");
        updatedTransaction.setTimestamp(Instant.now());

        when(transactionService.update(eq(testId), any(Transaction.class)))
                .thenReturn(updatedTransaction);

        mockMvc.perform(put("/api/transactions/" + testId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedTransaction)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testId.toString()))
                .andExpect(jsonPath("$.amount").value(200.00))
                .andExpect(jsonPath("$.type").value("WITHDRAWAL"))
                .andExpect(jsonPath("$.description").value("Updated transaction description"))
                .andExpect(jsonPath("$.category").value("Food"));
    }

    @Test
    @DisplayName("Should return not found when updating non-existent transaction")
    void testUpdateTransactionNotFound() throws Exception {
        when(transactionService.update(eq(testId), any(Transaction.class)))
                .thenThrow(new RuntimeException("Transaction not found"));

        mockMvc.perform(put("/api/transactions/" + testId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testTransaction)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should successfully delete existing transaction")
    void testDeleteTransaction() throws Exception {
        doNothing().when(transactionService).delete(testId);

        mockMvc.perform(delete("/api/transactions/" + testId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should return bad request when deleting non-existent transaction")
    void testDeleteTransactionNotFound() throws Exception {
        doThrow(new ApiException(TransactionErrorType.TRANSACTION_NOT_FOUND))
                .when(transactionService).delete(testId);

        mockMvc.perform(delete("/api/transactions/" + testId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}