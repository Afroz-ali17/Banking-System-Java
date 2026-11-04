package com.afroz.bank;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Demonstrates thread safety in the Banking System:
 * 1. Concurrent deposits proving mutual exclusion and race condition prevention.
 * 2. Deadlock-free concurrent bi-directional transfers using consistent lock ordering.
 */
public class ConcurrencyDemo {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("==================================================");
        System.out.println("        DAY 5: MULTITHREADING & CONCURRENCY       ");
        System.out.println("==================================================");

        demonstrateConcurrentDeposits();
        demonstrateDeadlockFreeTransfers();
    }

    /**
     * Demonstrates that synchronized deposit() protects against race conditions.
     * One account initialized with balance 0.
     * An ExecutorService with 10 threads runs 1,000 tasks each depositing 1.
     * Expected final balance is exactly 1000.00.
     */
    public static void demonstrateConcurrentDeposits() throws InterruptedException {
        System.out.println("\n--- 1. Concurrent Deposits (Thread Safety Proof) ---");
        Bank bank = new Bank();
        Account account = bank.createAccount("CURRENT", "Concurrency Tester", 0.0);
        System.out.println("Initial Account State: " + account);

        int numberOfThreads = 10;
        int totalTasks = 1000;
        double depositAmount = 1.0;

        System.out.printf("Submitting %d deposit tasks of %.2f across %d threads in ExecutorService...%n",
                totalTasks, depositAmount, numberOfThreads);

        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(totalTasks);

        long startTime = System.currentTimeMillis();
        for (int i = 0; i < totalTasks; i++) {
            executor.submit(() -> {
                try {
                    account.deposit(depositAmount);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(10, TimeUnit.SECONDS);
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);
        long duration = System.currentTimeMillis() - startTime;

        System.out.println("All deposit tasks completed in " + duration + " ms.");
        System.out.printf("Expected Balance : %.2f%n", (totalTasks * depositAmount));
        System.out.printf("Actual Balance   : %.2f%n", account.getBalance());
        System.out.println("Recorded History : " + account.getHistory().size() + " transactions");

        if (Double.compare(account.getBalance(), totalTasks * depositAmount) == 0) {
            System.out.println("[RESULT] SUCCESS: Zero lost updates! Synchronized deposit is thread-safe.");
        } else {
            System.out.println("[RESULT] FAILED: Race condition detected!");
        }
    }

    /**
     * Demonstrates deadlock prevention in transfer().
     * Two accounts: A ("CA2001") and B ("CA2002"), each starting with 10,000.
     * Thread 1 performs transfers A -> B repeatedly.
     * Thread 2 performs transfers B -> A repeatedly.
     * Because locks are acquired in consistent order (by account number), circular-wait
     * deadlock is eliminated and both threads finish cleanly.
     */
    public static void demonstrateDeadlockFreeTransfers() throws InterruptedException {
        System.out.println("\n--- 2. Deadlock-Free Concurrent Transfers ---");
        Bank bank = new Bank();
        Account accountA = bank.createAccount("CURRENT", "Alice", 10000.0);
        Account accountB = bank.createAccount("CURRENT", "Bob", 10000.0);

        String accA = accountA.getAccountNumber();
        String accB = accountB.getAccountNumber();

        System.out.println("Account A: " + accountA);
        System.out.println("Account B: " + accountB);
        double totalMoneyBefore = accountA.getBalance() + accountB.getBalance();
        System.out.printf("Total System Balance Before: %.2f%n", totalMoneyBefore);

        int transferCount = 1000;
        double amount = 5.0;

        CountDownLatch startSignal = new CountDownLatch(1);
        CountDownLatch doneSignal = new CountDownLatch(2);

        // Thread 1: Transfers from A to B
        Thread thread1 = new Thread(() -> {
            try {
                startSignal.await();
                for (int i = 0; i < transferCount; i++) {
                    bank.transfer(accA, accB, amount);
                }
            } catch (Exception e) {
                System.err.println("Thread 1 error: " + e.getMessage());
            } finally {
                doneSignal.countDown();
            }
        }, "Transfer-A-to-B");

        // Thread 2: Transfers from B to A
        Thread thread2 = new Thread(() -> {
            try {
                startSignal.await();
                for (int i = 0; i < transferCount; i++) {
                    bank.transfer(accB, accA, amount);
                }
            } catch (Exception e) {
                System.err.println("Thread 2 error: " + e.getMessage());
            } finally {
                doneSignal.countDown();
            }
        }, "Transfer-B-to-A");

        thread1.start();
        thread2.start();

        System.out.printf("Running concurrent bi-directional transfers (%d each A->B and B->A)...%n", transferCount);
        startSignal.countDown(); // Release both threads at the same instant

        boolean completed = doneSignal.await(10, TimeUnit.SECONDS);

        if (completed) {
            System.out.println("[RESULT] SUCCESS: All " + (transferCount * 2) + " transfers completed without deadlock!");
            System.out.println("Account A Final: " + accountA);
            System.out.println("Account B Final: " + accountB);
            double totalMoneyAfter = accountA.getBalance() + accountB.getBalance();
            System.out.printf("Total System Balance After : %.2f%n", totalMoneyAfter);
            System.out.printf("Conservation of Money Check: %s (%.2f == %.2f)%n",
                    (Double.compare(totalMoneyBefore, totalMoneyAfter) == 0 ? "PASSED" : "FAILED"),
                    totalMoneyBefore, totalMoneyAfter);
        } else {
            System.err.println("[RESULT] DEADLOCK DETECTED! Threads failed to complete within timeout.");
        }
    }
}

