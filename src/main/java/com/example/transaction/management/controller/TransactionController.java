package com.example.transaction.management.controller;

import com.example.transaction.management.model.Transaction;
import com.example.transaction.management.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactions")
@Tag(name = "Transaction Management", description = "Comprehensive API for managing financial transactions including CRUD operations")
public class TransactionController {
    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Operation(summary = "Create Transaction", description = "Create a new financial transaction record")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Transaction created successfully", 
                    content = @Content(schema = @Schema(implementation = Transaction.class))),
        @ApiResponse(responseCode = "400", description = "Invalid transaction data provided")
    })
    @PostMapping
    public ResponseEntity<Transaction> createTransaction(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Transaction details to be created", 
                required = true, 
                content = @Content(schema = @Schema(implementation = Transaction.class)))
            @Valid @RequestBody Transaction transaction) {
        return ResponseEntity.ok(transactionService.create(transaction));
    }

    @Operation(summary = "Retrieve Transaction", description = "Retrieve transaction details by unique identifier")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Transaction retrieved successfully", 
                    content = @Content(schema = @Schema(implementation = Transaction.class))),
        @ApiResponse(responseCode = "404", description = "Transaction not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Transaction> getTransaction(
            @Parameter(description = "Unique transaction identifier", required = true)
            @PathVariable Long id) {
        Optional<Transaction> transaction = transactionService.read(id);
        return transaction.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "List Transactions", description = "Retrieve paginated list of all transactions")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Transaction list retrieved successfully", 
                    content = @Content(schema = @Schema(implementation = Transaction.class)))
    })
    @GetMapping
    public ResponseEntity<List<Transaction>> getAllTransactions(
            @Parameter(description = "Page number (zero-based indexing)") 
            @RequestParam(defaultValue = "0") int page, 
            @Parameter(description = "Number of records per page") 
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(transactionService.list(page, size));
    }

    @Operation(summary = "Update Transaction", description = "Modify existing transaction information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Transaction updated successfully", 
                    content = @Content(schema = @Schema(implementation = Transaction.class))),
        @ApiResponse(responseCode = "404", description = "Transaction not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<Transaction> updateTransaction(
            @Parameter(description = "Unique transaction identifier", required = true)
            @PathVariable Long id, 
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Updated transaction information", 
                required = true, 
                content = @Content(schema = @Schema(implementation = Transaction.class)))
            @Valid @RequestBody Transaction transaction) {
        try {
            return ResponseEntity.ok(transactionService.update(id, transaction));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Delete Transaction", description = "Remove transaction from the system")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Transaction deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Transaction not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(
            @Parameter(description = "Unique transaction identifier", required = true)
            @PathVariable Long id) {
        transactionService.delete(id);
        return ResponseEntity.ok().build();
    }
}