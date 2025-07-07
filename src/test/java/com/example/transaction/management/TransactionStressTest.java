package com.example.transaction.management;

import com.example.transaction.management.model.Transaction;
import com.example.transaction.management.model.TransactionType;
import com.example.transaction.management.repository.TransactionRepository;
import com.example.transaction.management.service.TransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TransactionStressTest {

    // Stress test core parameters
    private static final int INITIAL_DATA_COUNT = 5_000;   // Initial data count
    private static final int STRESS_REQUEST_COUNT = 5_000;   // Requests per test
    private static final int TIMEOUT_SECONDS = 30;         // Timeout in seconds

    // Thread configuration for each interface test
    private static final int CREATE_THREADS = 20;          // Create threads
    private static final int READ_THREADS = 20;            // Read threads
    private static final int LIST_THREADS = 20;            // List threads
    private static final int UPDATE_THREADS = 20;          // Update threads
    private static final int DELETE_THREADS = 20;          // Delete threads
    private static final int MIXED_THREADS = 20;           // Mixed operation threads

    // Pagination parameters
    private static final int PAGE_SIZE = 50;

    // Test transaction data
    private static final BigDecimal BASE_AMOUNT = new BigDecimal("100.00");
    private static final BigDecimal UPDATED_AMOUNT = new BigDecimal("200.00");

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private TransactionRepository repository;

    @Autowired
    private ObjectMapper objectMapper;

    private String baseUrl;
    private HttpHeaders headers;
    private List<UUID> initialDataIds = new ArrayList<>();
    private Random random;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/transactions";
        headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        random = new Random();
        
        // Prepare initial data
        prepareInitialData();
    }

    /**
     * Prepare initial data - Use service directly to avoid HTTP overhead
     */
    private void prepareInitialData() {
        long startTime = System.currentTimeMillis();
        
        repository.clear();
        initialDataIds.clear();
        for (int i = 0; i < INITIAL_DATA_COUNT; i++) {
            Transaction transaction = new Transaction();
            transaction.setAmount(BASE_AMOUNT.add(new BigDecimal(i % 1000)));
            transaction.setType(TransactionType.values()[i % 3]);
            transaction.setDescription("Initial data transaction " + i);
            transaction.setCategory("Initial data category");

            initialDataIds.add(repository.save(transaction).getId());
        }
        
        long duration = System.currentTimeMillis() - startTime;
        
        assertTrue(initialDataIds.size() >= INITIAL_DATA_COUNT, 
            "Insufficient initial data, expected at least " + (INITIAL_DATA_COUNT) + " records, actual " + initialDataIds.size() + " records");
    }

    @Test
    @DisplayName("1. Create Transaction API Stress Test - POST /api/transactions")
    void testCreateTransactionStress() throws InterruptedException {
        System.out.println("\n=== Create Transaction API Stress Test ===");
        
        ExecutorService executor = Executors.newFixedThreadPool(CREATE_THREADS);
        CountDownLatch latch = new CountDownLatch(CREATE_THREADS);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        
        int requestsPerThread = STRESS_REQUEST_COUNT / CREATE_THREADS;
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < CREATE_THREADS; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < requestsPerThread; j++) {
                        if (performCreateOperation(threadId, j)) {
                            successCount.incrementAndGet();
                        } else {
                            failureCount.incrementAndGet();
                        }
                    }
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }
        
        assertTrue(latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS), 
            "Create transaction stress test timeout");
        executor.shutdown();
        
        printTestResults("Create Transaction", startTime, successCount.get(), failureCount.get());
        
        assertTrue(successCount.get() >= STRESS_REQUEST_COUNT, 
            "Create transaction success rate should be 100%");
    }

    @Test
    @DisplayName("2. Read Transaction API Stress Test - GET /api/transactions/{id}")
    void testReadTransactionStress() throws InterruptedException {
        System.out.println("\n=== Read Transaction API Stress Test ===");
        
        ExecutorService executor = Executors.newFixedThreadPool(READ_THREADS);
        CountDownLatch latch = new CountDownLatch(READ_THREADS);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        
        int requestsPerThread = STRESS_REQUEST_COUNT / READ_THREADS;
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < READ_THREADS; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < requestsPerThread; j++) {
                        if (performReadOperation()) {
                            successCount.incrementAndGet();
                        } else {
                            failureCount.incrementAndGet();
                        }
                    }
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }
        
        assertTrue(latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS), 
            "Read transaction stress test timeout");
        executor.shutdown();
        
        printTestResults("Read Transaction", startTime, successCount.get(), failureCount.get());
        
        assertTrue(successCount.get() >= STRESS_REQUEST_COUNT, 
            "Read transaction success rate should be 100%");
    }

    @Test
    @DisplayName("3. Transaction List API Stress Test - GET /api/transactions")
    void testListTransactionStress() throws InterruptedException {
        System.out.println("\n=== Transaction List API Stress Test ===");
        
        ExecutorService executor = Executors.newFixedThreadPool(LIST_THREADS);
        CountDownLatch latch = new CountDownLatch(LIST_THREADS);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        
        int requestsPerThread = STRESS_REQUEST_COUNT / LIST_THREADS;
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < LIST_THREADS; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < requestsPerThread; j++) {
                        if (performListOperation()) {
                            successCount.incrementAndGet();
                        } else {
                            failureCount.incrementAndGet();
                        }
                    }
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }
        
        assertTrue(latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS), 
            "Transaction list stress test timeout");
        executor.shutdown();
        
        printTestResults("Transaction List", startTime, successCount.get(), failureCount.get());
        
        assertTrue(successCount.get() >= STRESS_REQUEST_COUNT, 
            "Transaction list success rate should be 100%");
    }

    @Test
    @DisplayName("4. Update Transaction API Stress Test - PUT /api/transactions/{id}")
    void testUpdateTransactionStress() throws InterruptedException {
        System.out.println("\n=== Update Transaction API Stress Test ===");
        
        ExecutorService executor = Executors.newFixedThreadPool(UPDATE_THREADS);
        CountDownLatch latch = new CountDownLatch(UPDATE_THREADS);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        
        int requestsPerThread = STRESS_REQUEST_COUNT / UPDATE_THREADS;
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < UPDATE_THREADS; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < requestsPerThread; j++) {
                        if (performUpdateOperation(threadId, j)) {
                            successCount.incrementAndGet();
                        } else {
                            failureCount.incrementAndGet();
                        }
                    }
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }
        
        assertTrue(latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS), 
            "Update transaction stress test timeout");
        executor.shutdown();
        
        printTestResults("Update Transaction", startTime, successCount.get(), failureCount.get());
        
        assertTrue(successCount.get() >= STRESS_REQUEST_COUNT, 
            "Update transaction success rate should be 100%");
    }

    @Test
    @DisplayName("5. Delete Transaction API Stress Test - DELETE /api/transactions/{id}")
    void testDeleteTransactionStress() throws InterruptedException {
        System.out.println("\n=== Delete Transaction API Stress Test ===");
        
        ExecutorService executor = Executors.newFixedThreadPool(DELETE_THREADS);
        CountDownLatch latch = new CountDownLatch(DELETE_THREADS);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        
        int requestsPerThread = STRESS_REQUEST_COUNT / DELETE_THREADS;
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < DELETE_THREADS; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < requestsPerThread; j++) {
                        if (performDeleteOperation()) {
                            successCount.incrementAndGet();
                        } else {
                            failureCount.incrementAndGet();
                        }
                    }
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }
        
        assertTrue(latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS), 
            "Delete transaction stress test timeout");
        executor.shutdown();
        
        printTestResults("Delete Transaction", startTime, successCount.get(), failureCount.get());
        
        assertTrue(successCount.get() >= STRESS_REQUEST_COUNT, 
            "Delete transaction success rate should be 100%");
    }

    @Test
    @DisplayName("6. Mixed Operations API Stress Test - Random calls to 5 APIs")
    void testMixedOperationStress() throws InterruptedException {
        System.out.println("\n=== Mixed Operations API Stress Test ===");
        
        ExecutorService executor = Executors.newFixedThreadPool(MIXED_THREADS);
        CountDownLatch latch = new CountDownLatch(MIXED_THREADS);
        AtomicInteger createSuccess = new AtomicInteger(0);
        AtomicInteger readSuccess = new AtomicInteger(0);
        AtomicInteger listSuccess = new AtomicInteger(0);
        AtomicInteger updateSuccess = new AtomicInteger(0);
        AtomicInteger deleteSuccess = new AtomicInteger(0);
        AtomicInteger totalFailure = new AtomicInteger(0);
        
        int requestsPerThread = STRESS_REQUEST_COUNT / MIXED_THREADS;
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < MIXED_THREADS; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < requestsPerThread; j++) {
                        // Randomly select one of the 5 APIs
                        int operation = random.nextInt(5);
                        
                        try {
                            switch (operation) {
                                case 0: // CREATE
                                    if (performCreateOperation(threadId, j)) {
                                        createSuccess.incrementAndGet();
                                    } else {
                                        totalFailure.incrementAndGet();
                                    }
                                    break;
                                case 1: // READ
                                    if (performReadOperation()) {
                                        readSuccess.incrementAndGet();
                                    } else {
                                        totalFailure.incrementAndGet();
                                    }
                                    break;
                                case 2: // LIST
                                    if (performListOperation()) {
                                        listSuccess.incrementAndGet();
                                    } else {
                                        totalFailure.incrementAndGet();
                                    }
                                    break;
                                case 3: // UPDATE
                                    if (performUpdateOperation(threadId, j)) {
                                        updateSuccess.incrementAndGet();
                                    } else {
                                        totalFailure.incrementAndGet();
                                    }
                                    break;
                                case 4: // DELETE
                                    if (performDeleteOperation()) {
                                        deleteSuccess.incrementAndGet();
                                    } else {
                                        totalFailure.incrementAndGet();
                                    }
                                    break;
                            }
                        } catch (Exception e) {
                            System.out.println("Mixed operation execution exception: " + e.getMessage());
                            e.printStackTrace();
                            totalFailure.incrementAndGet();
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        
        assertTrue(latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS), 
            "Mixed operations stress test timeout");
        executor.shutdown();
        
        int totalSuccess = createSuccess.get() + readSuccess.get() + listSuccess.get() + 
                          updateSuccess.get() + deleteSuccess.get();
        
        printMixedTestResults(startTime, createSuccess.get(), readSuccess.get(), 
                            listSuccess.get(), updateSuccess.get(), deleteSuccess.get(), 
                            totalFailure.get());
        
        assertTrue(totalSuccess >= STRESS_REQUEST_COUNT, 
            "Mixed operations overall success rate should be 100%");
    }

    // ===== Helper methods =====
    
    private UUID getRandomTransactionId() {
        if (initialDataIds.isEmpty()) {
            throw new RuntimeException("Initial data is empty, cannot get random ID");
        }
        return initialDataIds.get(random.nextInt(initialDataIds.size()));
    }

    private boolean performCreateOperation(int threadId, int index) {
        try {
            Transaction transaction = new Transaction();
            transaction.setAmount(BASE_AMOUNT.add(new BigDecimal(index)));
            transaction.setType(TransactionType.values()[index % 3]);
            transaction.setDescription("Stress test transaction " + threadId + "-" + index);
            transaction.setCategory("Stress test category");
            
            HttpEntity<Transaction> request = new HttpEntity<>(transaction, headers);
            ResponseEntity<Transaction> response = restTemplate.postForEntity(baseUrl, request, Transaction.class);
            return processResponse(response);
        } catch (Exception e) {
            System.out.println("Create operation exception - Thread ID: " + threadId + ", Index: " + index + ", Exception: " + e.getMessage());
            return false;
        }
    }

    private boolean performReadOperation() {
        try {
            UUID randomId = getRandomTransactionId();
            ResponseEntity<Transaction> response = restTemplate.getForEntity(baseUrl + "/" + randomId, Transaction.class);
            return processResponse(response);
        } catch (Exception e) {
            System.out.println("Read operation exception - Exception: " + e.getMessage());
            return false;
        }
    }

    private boolean performListOperation() {
        try {
            int page = random.nextInt(20);
            ResponseEntity<Transaction[]> response = restTemplate.getForEntity(
                baseUrl + "?page=" + page + "&size=" + PAGE_SIZE, Transaction[].class);
            return processResponse(response);
        } catch (Exception e) {
            System.out.println("List operation exception - Exception: " + e.getMessage());
            return false;
        }
    }

    private boolean performUpdateOperation(int threadId, int index) {
        try {
            UUID randomId = getRandomTransactionId();
            Transaction updateTransaction = new Transaction();
            updateTransaction.setAmount(UPDATED_AMOUNT.add(new BigDecimal(index)));
            updateTransaction.setType(TransactionType.values()[(index + 1) % 3]);
            updateTransaction.setDescription("Updated transaction " + threadId + "-" + index);
            updateTransaction.setCategory("Updated category");
            HttpEntity<Transaction> request = new HttpEntity<>(updateTransaction, headers);
            ResponseEntity<Transaction> response = restTemplate.exchange(
                baseUrl + "/" + randomId, HttpMethod.PUT, request, Transaction.class);
            return processResponse(response);
        } catch (Exception e) {
            System.out.println("Update operation exception - Thread ID: " + threadId + ", Index: " + index + ", Exception: " + e.getMessage());
            return false;
        }
    }

    private boolean performDeleteOperation() {
        try {
            UUID randomId = getRandomTransactionId();
            ResponseEntity<Void> response = restTemplate.exchange(
                baseUrl + "/" + randomId, HttpMethod.DELETE, null, Void.class);
            return processResponse(response);
        } catch (Exception e) {
            System.out.println("Delete operation exception - Exception: " + e.getMessage());
            return false;
        }
    }

    private void printTestResults(String testName, long startTime, int success, int failure) {
        long duration = System.currentTimeMillis() - startTime;
        double throughput = (double) success / (duration / 1000.0);
        int total = success + failure;
        double successRate = (double) success / total * 100;
        
        System.out.println(testName + " Stress Test Results:");
        System.out.println("- Total Requests: " + total);
        System.out.println("- Successful Requests: " + success);
        System.out.println("- Failed Requests: " + failure);
        System.out.println("- Success Rate: " + String.format("%.2f", successRate) + "%");
        System.out.println("- Total Time: " + duration + "ms");
        System.out.println("- Average Throughput: " + String.format("%.2f", throughput) + " requests/sec");
    }

    private void printMixedTestResults(long startTime, int createSuccess, int readSuccess, 
                                     int listSuccess, int updateSuccess, int deleteSuccess, 
                                     int totalFailure) {
        long duration = System.currentTimeMillis() - startTime;
        int totalSuccess = createSuccess + readSuccess + listSuccess + updateSuccess + deleteSuccess;
        int total = totalSuccess + totalFailure;
        double throughput = (double) totalSuccess / (duration / 1000.0);
        double successRate = (double) totalSuccess / total * 100;
        
        System.out.println("Mixed Operations Stress Test Results:");
        System.out.println("- Total Requests: " + total);
        System.out.println("- Create Success Count: " + createSuccess);
        System.out.println("- Read Success Count: " + readSuccess);
        System.out.println("- List Success Count: " + listSuccess);
        System.out.println("- Update Success Count: " + updateSuccess);
        System.out.println("- Delete Success Count: " + deleteSuccess);
        System.out.println("- Failed Requests: " + totalFailure);
        System.out.println("- Overall Success Rate: " + String.format("%.2f", successRate) + "%");
        System.out.println("- Total Time: " + duration + "ms");
        System.out.println("- Average Throughput: " + String.format("%.2f", throughput) + " operations/sec");
    }

    private boolean processResponse(ResponseEntity<?> response) {
        boolean result = response.getStatusCode() == HttpStatus.OK || 
            response.getStatusCode() == HttpStatus.NOT_FOUND;
        if (!result) {
            System.out.println("Process response - Status code: " + response.getStatusCode() + ", Result: " + response.getBody());
        }
        return result;
    }
} 