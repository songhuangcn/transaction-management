package com.example.transaction.management.exception;

public enum TransactionErrorType {
    TRANSACTION_NOT_FOUND("Requested transaction was not found"),
    INVALID_PAGINATION("Pagination parameters are invalid");

    private final String message;

    TransactionErrorType(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
