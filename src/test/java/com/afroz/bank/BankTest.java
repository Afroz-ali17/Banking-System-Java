package com.afroz.bank;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BankTest {

    private Bank bank;

    @BeforeEach
    void setUp() {
        bank = new Bank();
    }

    @Test
    @DisplayName("createAccount generates SB1001 for savings and CA2001 for current account")
    void testAccountCreationAndNumbering() {
        Account sb1 = bank.createAccount("SAVINGS", "Alice", 1000);
        Account ca1 = bank.createAccount("CURRENT", "Bob", 2000);
        Account sb2 = bank.createAccount("SAVINGS", "Charlie", 500);
        Account ca2 = bank.createAccount("CURRENT", "Diana", 0);

        assertEquals("SB1001", sb1.getAccountNumber());
        assertEquals("CA2001", ca1.getAccountNumber());
        assertEquals("SB1002", sb2.getAccountNumber());
        assertEquals("CA2002", ca2.getAccountNumber());
        assertEquals(4, bank.getAccountCount());
    }

    @Test
    @DisplayName("createAccount rejects Savings opening balance below 500")
    void testSavingsMinimumOpeningBalanceValidation() {
        // Exactly at minimum threshold (500) must succeed
        assertDoesNotThrow(() -> bank.createAccount("SAVINGS", "Valid", 500));

        // Below threshold must fail with IllegalArgumentException
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> bank.createAccount("SAVINGS", "Invalid", 499.99));
        assertTrue(ex.getMessage().contains("at least 500"));
    }

    @Test
    @DisplayName("createAccount rejects invalid account types and negative current account balance")
    void testInvalidAccountTypeAndNegativeBalance() {
        assertThrows(IllegalArgumentException.class,
                () -> bank.createAccount("INVESTMENT", "Eve", 1000));
        assertThrows(IllegalArgumentException.class,
                () -> bank.createAccount("CURRENT", "Frank", -100));
    }

    @Test
    @DisplayName("getAccount retrieves an existing account in O(1) time")
    void testGetAccountSuccess() throws AccountNotFoundException {
        Account created = bank.createAccount("SAVINGS", "Grace", 2500);
        Account retrieved = bank.getAccount("SB1001");

        assertNotNull(retrieved);
        assertSame(created, retrieved);
        assertEquals("Grace", retrieved.getHolderName());
    }

    @Test
    @DisplayName("getAccount throws checked AccountNotFoundException when account does not exist")
    void testGetAccountNotFound() {
        assertThrows(AccountNotFoundException.class, () -> bank.getAccount("SB9999"));
        assertThrows(AccountNotFoundException.class, () -> bank.getAccount(null));
    }

    @Test
    @DisplayName("closeAccount only works if the balance is 0")
    void testCloseAccountBalanceRules() throws AccountNotFoundException, InsufficientBalanceException {
        Account current = bank.createAccount("CURRENT", "Henry", 1000);
        String accNo = current.getAccountNumber();

        // 1. Account has positive balance (1000) -> closeAccount must fail (returns false)
        boolean closedWithBalance = bank.closeAccount(accNo);
        assertFalse(closedWithBalance, "Account closure should fail when balance is non-zero");
        assertNotNull(bank.getAccount(accNo), "Account must still exist in the bank");

        // 2. Withdraw full amount to reach 0 balance
        current.withdraw(1000);
        assertEquals(0.0, current.getBalance());

        // 3. Now closure should succeed (returns true)
        boolean closedZero = bank.closeAccount(accNo);
        assertTrue(closedZero, "Account closure should succeed when balance is exactly 0");

        // 4. Verifying account is removed from bank
        assertThrows(AccountNotFoundException.class, () -> bank.getAccount(accNo));
    }

    @Test
    @DisplayName("closeAccount throws AccountNotFoundException for non-existent accounts")
    void testCloseAccountNotFound() {
        assertThrows(AccountNotFoundException.class, () -> bank.closeAccount("CA9999"));
    }

    @Test
    @DisplayName("listAccounts returns all registered accounts")
    void testListAccounts() {
        bank.createAccount("SAVINGS", "Ivy", 600);
        bank.createAccount("CURRENT", "Jack", 1500);

        List<Account> accounts = bank.listAccounts();
        assertEquals(2, accounts.size());
    }
}

