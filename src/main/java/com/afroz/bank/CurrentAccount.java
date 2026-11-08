package com.afroz.bank;

public class CurrentAccount extends Account {
    private static final double OVERDRAFT_LIMIT = 5000;

    public CurrentAccount(String accNo, String name, double opening) {
        super(accNo, name, opening);
        if (Double.isNaN(opening) || Double.isInfinite(opening) || opening < 0) {
            throw new IllegalArgumentException("Current account opening balance cannot be negative or invalid. Provided: " + opening);
        }
    }

    @Override
    public synchronized void withdraw(double amount, String transactionType) throws InsufficientBalanceException {
        if (Double.isNaN(amount) || Double.isInfinite(amount) || amount <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive and finite. Provided: " + amount);
        }
        if (balance + OVERDRAFT_LIMIT < amount)
            throw new InsufficientBalanceException("Overdraft limit of " + OVERDRAFT_LIMIT + " exceeded");
        balance -= amount;
        record(transactionType, amount);
    }
}
