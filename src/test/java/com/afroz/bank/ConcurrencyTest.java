package com.afroz.bank;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class ConcurrencyTest {

    @Test
    @DisplayName("Concurrent deposits across 10 threads are atomic and prevent lost updates")
    void testConcurrentDepositsThreadSafety() throws InterruptedException {
        Bank bank = new Bank();
        Account account = bank.createAccount("CURRENT", "ThreadSafe Test", 0.0);

        int threadCount = 10;
        int tasks = 1000;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(tasks);

        for (int i = 0; i < tasks; i++) {
            executor.submit(() -> {
                try {
                    account.deposit(1.0);
                } finally {
                    latch.countDown();
                }
            });
        }

        assertTrue(latch.await(5, TimeUnit.SECONDS), "Deposit tasks timed out");
        executor.shutdown();
        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));

        assertEquals(1000.0, account.getBalance(), "Final balance must exactly equal 1000.00 with zero lost updates");
        assertEquals(1000, account.getHistory().size(), "Transaction history must capture all 1000 deposits");
    }

    @Test
    @DisplayName("Bi-directional transfers run concurrently without deadlock due to consistent lock ordering")
    void testBiDirectionalTransfersDeadlockFree() throws InterruptedException {
        Bank bank = new Bank();
        Account accA = bank.createAccount("CURRENT", "Alice", 5000.0);
        Account accB = bank.createAccount("CURRENT", "Bob", 5000.0);

        String idA = accA.getAccountNumber();
        String idB = accB.getAccountNumber();

        int iterations = 500;
        double amount = 2.0;

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(2);

        Thread t1 = new Thread(() -> {
            try {
                startLatch.await();
                for (int i = 0; i < iterations; i++) {
                    bank.transfer(idA, idB, amount);
                }
            } catch (Exception e) {
                fail("Thread 1 encountered unexpected error: " + e.getMessage());
            } finally {
                finishLatch.countDown();
            }
        });

        Thread t2 = new Thread(() -> {
            try {
                startLatch.await();
                for (int i = 0; i < iterations; i++) {
                    bank.transfer(idB, idA, amount);
                }
            } catch (Exception e) {
                fail("Thread 2 encountered unexpected error: " + e.getMessage());
            } finally {
                finishLatch.countDown();
            }
        });

        t1.start();
        t2.start();
        startLatch.countDown(); // release both threads simultaneously

        // Must complete in under 5 seconds; if deadlock occurs, it will time out and fail
        boolean completed = finishLatch.await(5, TimeUnit.SECONDS);
        assertTrue(completed, "Bi-directional transfers deadlocked or timed out!");

        // Total money in the system must remain conserved: 5000 + 5000 = 10000
        assertEquals(10000.0, accA.getBalance() + accB.getBalance(), "Total system balance must be conserved");
    }
}

