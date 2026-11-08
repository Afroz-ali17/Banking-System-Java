package com.afroz.bank;

public class SavingsAccount extends Account {
    private static final double MIN_BALANCE = 500;

    public SavingsAccount(String accNo, String name, double opening) {
        super(accNo, name, opening);
        if (Double.isNaN(opening) || Double.isInfinite(opening) || opening < MIN_BALANCE) {
            throw new IllegalArgumentException("Savings account requires an opening balance of at least " + MIN_BALANCE + ". Provided: " + opening);
        }
    }

    @Override
    public synchronized void withdraw(double amount, String transactionType) throws InsufficientBalanceException {
        if (Double.isNaN(amount) || Double.isInfinite(amount) || amount <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive and finite. Provided: " + amount);
        }
        if (balance - amount < MIN_BALANCE)
            throw new InsufficientBalanceException("Savings must keep a minimum balance of " + MIN_BALANCE);
        balance -= amount;
        record(transactionType, amount);
    }
}
