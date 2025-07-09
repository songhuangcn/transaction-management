package com.example.transaction.management.service;

import com.example.transaction.management.exception.ApiException;
import com.example.transaction.management.exception.TransactionErrorType;
import com.example.transaction.management.model.Transaction;
import com.example.transaction.management.repository.TransactionRepository;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
public class TransactionService {
    private static final int MAX_PAGE_SIZE = 50;
    private final TransactionRepository repository;
    private final ReentrantLock lock = new ReentrantLock();

    public TransactionService(TransactionRepository repository) {
        this.repository = repository;
    }

    public Transaction create(@Valid Transaction transaction) {
        return repository.save(transaction);
    }

    @Cacheable(value = "transactions", key = "#id")
    public Optional<Transaction> read(Long id) {
        return repository.findById(id);
    }

    public List<Transaction> list(int page, int size) {
        validatePagination(page, size);
        return repository.findAll(page, size);
    }

    @CacheEvict(value = "transactions", key = "#id")
    public Transaction update(Long id, @Valid Transaction transaction) {
        try {
            lock.lock();
            if (repository.findById(id).isEmpty()) {
                throw new ApiException(TransactionErrorType.TRANSACTION_NOT_FOUND, HttpStatus.NOT_FOUND);
            }
            transaction.setId(id);
            return repository.save(transaction);
        } finally {
            lock.unlock();
        }
    }

    @CacheEvict(value = "transactions", key = "#id")
    public void delete(Long id) {
        try {
            lock.lock();
            if (repository.findById(id).isEmpty()) {
                throw new ApiException(TransactionErrorType.TRANSACTION_NOT_FOUND, HttpStatus.NOT_FOUND);
            }
            repository.deleteById(id);
        } finally {
            lock.unlock();
        }
    }

    private void validatePagination(int page, int size) {
        if (page < 0) {
            throw new ApiException(TransactionErrorType.INVALID_PAGINATION);
        }
        if (size <= 0) {
            throw new ApiException(TransactionErrorType.INVALID_PAGINATION);
        }
        if (size > MAX_PAGE_SIZE) {
            throw new ApiException(TransactionErrorType.INVALID_PAGINATION);
        }
    }
}