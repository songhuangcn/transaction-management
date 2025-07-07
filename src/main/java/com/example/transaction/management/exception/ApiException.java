package com.example.transaction.management.exception;

import org.springframework.http.HttpStatus;
import java.io.Serial;

public class ApiException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;
    
    private final HttpStatus status;
    private final TransactionErrorType errorType;

    public ApiException(TransactionErrorType errorType) {
        super(errorType.getMessage());
        this.status = HttpStatus.BAD_REQUEST;
        this.errorType = errorType;
    }

    public ApiException(TransactionErrorType errorType, HttpStatus status) {
        super(errorType.getMessage());
        this.status = status;
        this.errorType = errorType;
    }

    public ApiException(TransactionErrorType errorType, HttpStatus status, Throwable cause) {
        super(errorType.getMessage(), cause);
        this.status = status;
        this.errorType = errorType;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public TransactionErrorType getErrorType() {
        return errorType;
    }
} 