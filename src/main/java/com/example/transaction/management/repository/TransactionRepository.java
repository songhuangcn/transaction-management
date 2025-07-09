package com.example.transaction.management.repository;

import com.example.transaction.management.model.Transaction;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository {
    Transaction save(Transaction transaction);
    Optional<Transaction> findById(Long id);
    List<Transaction> findAll(int page, int size);
    void deleteById(Long id);
    long count();
    void clear();
} 