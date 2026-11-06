package com.afroz.bank;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AccountTest {

    @Test
    @DisplayName("deposit increases balance")
    void testDepositIncreasesBalance() {
        Account account = new SavingsAccount("SB1001", "Afroz", 1000);
        account.deposit(500);

        // Verify balance arithmetic and audit trail capture
        assertEquals(1500, account.getBalance());
        assertEquals(1, account.getHistory().size());
    }

    @Test
    @DisplayName("Deposit rejects zero or negative amounts to prevent balance tampering")
    void testDepositInvalidAmount() {
        Account account = new SavingsAccount("SB1001", "Afroz", 1000);
        assertThrows(IllegalArgumentException.class, () -> account.deposit(-50));
        assertThrows(IllegalArgumentException.class, () -> account.deposit(0));
    }

    @Test
    @DisplayName("Savings account allows withdrawal up to minimum balance limit")
    void testSavingsWithdrawSuccess() throws InsufficientBalanceException {
        Account account = new SavingsAccount("SB1001", "Afroz", 1000);
        account.withdraw(400); // 1000 - 400 = 600 >= MIN_BALANCE (500)

        assertEquals(600, account.getBalance());
        assertEquals(1, account.getHistory().size());
    }

    @Test
    @DisplayName("Savings rejects a withdrawal below the minimum balance")
    void testSavingsRejectsWithdrawalBelowMinimumBalance() {
        Account account = new SavingsAccount("SB1001", "Afroz", 1000);
        // Attempting to withdraw 600 leaves 400, violating minimum balance of 500
        assertThrows(InsufficientBalanceException.class, () -> account.withdraw(600));
    }

    @Test
    @DisplayName("Current allows overdraft within the limit")
    void testCurrentAllowsOverdraftWithinLimit() throws InsufficientBalanceException {
        Account account = new CurrentAccount("CA2001", "Ali", 2000);
        account.withdraw(6000); // 2000 + 5000 overdraft = 7000 max capacity

        // Balance drops into negative territory representing credit used
        assertEquals(-4000, account.getBalance());
    }

    @Test
    @DisplayName("Current account rejects withdrawal exceeding overdraft capacity")
    void testCurrentAccountOverdraftExceeded() {
        Account account = new CurrentAccount("CA2001", "Ali", 2000);
        // 2000 balance + 5000 overdraft limit = 7000 limit, so 7500 must fail
        assertThrows(InsufficientBalanceException.class, () -> account.withdraw(7500));
    }
}
