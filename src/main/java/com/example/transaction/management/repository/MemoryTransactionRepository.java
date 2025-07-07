package com.example.transaction.management.repository;

import com.example.transaction.management.model.Transaction;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

@Repository
public class MemoryTransactionRepository implements TransactionRepository {
    private final Map<UUID, Transaction> transactions = new ConcurrentHashMap<>();

    @Override
    public Transaction save(Transaction transaction) {
        if (transaction.getId() == null) {
            transaction.setId(UUID.randomUUID());
        }
        if (transaction.getTimestamp() == null) {
            transaction.setTimestamp(Instant.now());
        }
        transactions.put(transaction.getId(), transaction);
        return transaction;
    }

    @Override
    public Optional<Transaction> findById(UUID id) {
        return Optional.ofNullable(transactions.get(id));
    }

    @Override
    public List<Transaction> findAll(int page, int size) {        
        List<Transaction> snapshot = new ArrayList<>(transactions.values());
        return snapshot.stream()
                .sorted(Comparator.comparing(Transaction::getTimestamp).reversed())
                .skip((long) page * size)
                .limit(size)
                .collect(Collectors.toList());
    }

    @Override
    public long count() {
        return transactions.size();
    }

    @Override
    public void deleteById(UUID id) {
        transactions.remove(id);
    }

    @Override
    public void clear() {
        transactions.clear();
    }
}