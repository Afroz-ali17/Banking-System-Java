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
        if (amount <= 0) throw new IllegalArgumentException("Deposit must be positive");
        balance += amount;
        history.add(new Transaction("DEPOSIT", amount));
    }

    // Each account type defines its own withdrawal rule (polymorphism)
    public abstract void withdraw(double amount) throws InsufficientBalanceException;

    protected void record(String type, double amount) {
        history.add(new Transaction(type, amount));
    }

    @Override
    public String toString() {
        return accountNumber + " | " + holderName + " | Balance: " + balance;
    }
}
