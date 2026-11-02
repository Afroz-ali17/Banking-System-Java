package com.afroz.bank;

public class SavingsAccount extends Account {
    private static final double MIN_BALANCE = 500;

    public SavingsAccount(String accNo, String name, double opening) {
        super(accNo, name, opening);
    }

    @Override
    public synchronized void withdraw(double amount, String transactionType) throws InsufficientBalanceException {
        if (amount <= 0) throw new IllegalArgumentException("Withdrawal must be positive");
        if (balance - amount < MIN_BALANCE)
            throw new InsufficientBalanceException("Savings must keep a minimum balance of " + MIN_BALANCE);
        balance -= amount;
        record(transactionType, amount);
    }
}
