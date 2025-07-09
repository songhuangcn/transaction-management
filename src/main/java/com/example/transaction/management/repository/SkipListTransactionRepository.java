package com.example.transaction.management.repository;

import com.example.transaction.management.model.Transaction;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

@Repository
public class SkipListTransactionRepository implements TransactionRepository {
    // Use ConcurrentSkipListMap with reverse order to store transactions by id in descending order
    private final NavigableMap<Long, Transaction> transactions = new ConcurrentSkipListMap<>(Collections.reverseOrder());
    // Auto-increment ID generator starting from 1000
    private final AtomicLong idGenerator = new AtomicLong(1000);

    @Override
    public Transaction save(Transaction transaction) {
        if (transaction.getId() == null) {
            transaction.setId(idGenerator.incrementAndGet());
        }
        if (transaction.getTimestamp() == null) {
            transaction.setTimestamp(Instant.now());
        }
        transactions.put(transaction.getId(), transaction);
        return transaction;
    }

    @Override
    public Optional<Transaction> findById(Long id) {
        return Optional.ofNullable(transactions.get(id));
    }

    @Override
    public List<Transaction> findAll(int page, int size) {
        // Leverage the ordered nature of ConcurrentSkipListMap to get transactions in descending order by id
        return transactions.values()
                .stream()
                .skip((long) page * size)
                .limit(size)
                .collect(Collectors.toList());
    }

    @Override
    public long count() {
        return transactions.size();
    }

    @Override
    public void deleteById(Long id) {
        transactions.remove(id);
    }

    @Override
    public void clear() {
        transactions.clear();
        idGenerator.set(1000); // Reset ID generator
    }
}