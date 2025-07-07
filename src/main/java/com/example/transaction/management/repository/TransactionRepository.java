package com.example.transaction.management.repository;

import com.example.transaction.management.model.Transaction;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository {
    Transaction save(Transaction transaction);
    Optional<Transaction> findById(UUID id);
    List<Transaction> findAll(int page, int size);
    void deleteById(UUID id);
    long count();
    void clear();
} 