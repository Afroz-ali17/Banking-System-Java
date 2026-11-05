package com.afroz.bank;

import java.util.ArrayList;
import java.util.List;

public abstract class Account {
    private final String accountNumber;
    private final String holderName;
    protected double balance;
    private final List<Transaction> history = new ArrayList<>();

    protected Account(String accountNumber, String holderName, double openingBalance) {
        this.accountNumber = accountNumber;
        this.holderName = holderName;
        this.balance = openingBalance;
    }

    public String getAccountNumber() { return accountNumber; }
    public String getHolderName() { return holderName; }
    public double getBalance() { return balance; }
    public List<Transaction> getHistory() { return history; }

    public synchronized void deposit(double amount) {
        deposit(amount, "DEPOSIT");
    }

    public synchronized void deposit(double amount, String transactionType) {
        if (amount <= 0) throw new IllegalArgumentException("Deposit must be positive");
        balance += amount;
        record(transactionType, amount);
    }

    // Default withdrawal records standard WITHDRAW
    public void withdraw(double amount) throws InsufficientBalanceException {
        withdraw(amount, "WITHDRAW");
    }

    // Each account type defines its own withdrawal rule (polymorphism) and records custom transaction type
    public abstract void withdraw(double amount, String transactionType) throws InsufficientBalanceException;

    public synchronized void transferOut(double amount) throws InsufficientBalanceException {
        withdraw(amount, "TRANSFER_OUT");
    }

    public synchronized void transferIn(double amount) {
        deposit(amount, "TRANSFER_IN");
    }

    /**
     * Rolls back a failed transfer withdrawal by restoring the balance
     * and removing the recorded TRANSFER_OUT transaction.
     */
    public synchronized void restoreBalance(double amount) {
        balance += amount;
        if (!history.isEmpty()) {
            Transaction last = history.get(history.size() - 1);
            if ("TRANSFER_OUT".equals(last.getType())) {
                history.remove(history.size() - 1);
            }
        }
    }

    protected void record(String type, double amount) {
        history.add(new Transaction(type, amount));
    }

    /**
     * Appends a transaction loaded from persistent storage into history.
     */
    public synchronized void addHistoricalTransaction(Transaction transaction) {
        if (transaction != null) {
            history.add(transaction);
        }
    }

    @Override
    public String toString() {
        return accountNumber + " | " + holderName + " | Balance: " + balance;
    }
}
