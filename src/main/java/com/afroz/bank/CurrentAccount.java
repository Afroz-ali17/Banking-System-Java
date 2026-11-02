package com.afroz.bank;

public class CurrentAccount extends Account {
    private static final double OVERDRAFT_LIMIT = 5000;

    public CurrentAccount(String accNo, String name, double opening) {
        super(accNo, name, opening);
    }

    @Override
    public synchronized void withdraw(double amount, String transactionType) throws InsufficientBalanceException {
        if (amount <= 0) throw new IllegalArgumentException("Withdrawal must be positive");
        if (balance + OVERDRAFT_LIMIT < amount)
            throw new InsufficientBalanceException("Overdraft limit of " + OVERDRAFT_LIMIT + " exceeded");
        balance -= amount;
        record(transactionType, amount);
    }
}
