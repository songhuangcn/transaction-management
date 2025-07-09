package com.example.transaction.management.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;

@Schema(description = "Financial Transaction Entity")
public class Transaction {
    @Schema(description = "Unique transaction identifier", example = "1001")
    private Long id;
    
    @Schema(description = "Transaction amount in decimal format", example = "100.50")
    @NotNull(message = "Transaction amount is required")
    @DecimalMin(value = "0.01", message = "Transaction amount must be greater than zero")
    private BigDecimal amount;
    
    @Schema(description = "Type of financial transaction", example = "DEPOSIT", allowableValues = {"DEPOSIT", "WITHDRAWAL", "TRANSFER"})
    @NotNull(message = "Transaction type is required")
    private TransactionType type;
    
    @Schema(description = "Human-readable description of the transaction", example = "Salary deposit")
    @NotBlank(message = "Transaction description is required")
    private String description;
    
    @Schema(description = "Transaction category for classification", example = "Income")
    @NotBlank(message = "Transaction category is required")
    private String category;
    
    @Schema(description = "Timestamp when transaction was created", example = "2025-07-07T10:00:00Z")
    private Instant timestamp;

    public Transaction() {
        this.timestamp = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}