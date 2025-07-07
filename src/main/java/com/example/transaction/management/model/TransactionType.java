package com.example.transaction.management.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Financial Transaction Type Classification")
public enum TransactionType {
    @Schema(description = "Money deposited into account")
    DEPOSIT,
    
    @Schema(description = "Money withdrawn from account")
    WITHDRAWAL,
    
    @Schema(description = "Money transferred between accounts")
    TRANSFER
}