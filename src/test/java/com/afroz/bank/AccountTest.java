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

    @Test
    @DisplayName("Account history list is unmodifiable to protect internal encapsulation")
    void testHistoryEncapsulation() {
        Account account = new SavingsAccount("SB1001", "Afroz", 1000);
        account.deposit(200);
        var history = account.getHistory();
        assertThrows(UnsupportedOperationException.class, () -> history.add(new Transaction("TEST", 100)));
        assertThrows(UnsupportedOperationException.class, history::clear);
    }

    @Test
    @DisplayName("Deposit and withdraw reject NaN and infinite amounts")
    void testRejectNaNAndInfiniteAmounts() {
        Account account = new SavingsAccount("SB1001", "Afroz", 1000);
        assertThrows(IllegalArgumentException.class, () -> account.deposit(Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> account.deposit(Double.POSITIVE_INFINITY));
        assertThrows(IllegalArgumentException.class, () -> account.withdraw(Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> account.withdraw(Double.POSITIVE_INFINITY));
    }

    @Test
    @DisplayName("SavingsAccount constructor directly enforces minimum balance of 500")
    void testSavingsAccountConstructorValidation() {
        assertThrows(IllegalArgumentException.class, () -> new SavingsAccount("SB1001", "Afroz", 499.99));
        assertDoesNotThrow(() -> new SavingsAccount("SB1001", "Afroz", 500.00));
    }

    @Test
    @DisplayName("CurrentAccount constructor rejects negative opening balance")
    void testCurrentAccountConstructorValidation() {
        assertThrows(IllegalArgumentException.class, () -> new CurrentAccount("CA2001", "Ali", -1.0));
        assertDoesNotThrow(() -> new CurrentAccount("CA2001", "Ali", 0.0));
    }

    @Test
    @DisplayName("Account equals and hashCode obey entity equality based on accountNumber")
    void testEqualsAndHashCode() {
        Account a1 = new SavingsAccount("SB1001", "Afroz", 1000);
        Account a2 = new SavingsAccount("SB1001", "Afroz Different", 2000);
        Account a3 = new SavingsAccount("SB1002", "Afroz", 1000);

        assertEquals(a1, a2, "Accounts with identical account numbers must be equal");
        assertEquals(a1.hashCode(), a2.hashCode(), "Equal accounts must produce identical hash codes");
        assertNotEquals(a1, a3);
    }
}
