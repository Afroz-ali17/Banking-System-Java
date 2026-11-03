package com.afroz.bank;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

class BankAppTest {

    private String runBankAppWithInputs(String inputData, Bank bank) {
        ByteArrayInputStream in = new ByteArrayInputStream(inputData.getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(out));

        try {
            Scanner scanner = new Scanner(in);
            BankApp app = new BankApp(bank, scanner);
            app.start();
        } finally {
            System.setOut(originalOut);
        }
        return out.toString(StandardCharsets.UTF_8);
    }

    @Test
    @DisplayName("Menu action 1 creates an account and action 5 checks its balance")
    void testCreateAccountAndCheckBalance() {
        Bank bank = new Bank();
        // 1 (create) -> type "1" (SAVINGS), name "Afroz", opening 1500
        // 5 (check balance) -> accNo "SB1001"
        // 0 (exit)
        String inputs = String.join(System.lineSeparator(),
                "1", "1", "Afroz", "1500",
                "5", "SB1001",
                "0"
        ) + System.lineSeparator();

        String output = runBankAppWithInputs(inputs, bank);

        assertTrue(output.contains("Account created successfully!"));
        assertTrue(output.contains("SB1001"));
        assertTrue(output.contains("Current Balance: 1500.00"));
        assertTrue(output.contains("Goodbye!"));
    }

    @Test
    @DisplayName("Menu actions 2, 3, and 6 perform deposit, withdrawal, and mini statement")
    void testDepositWithdrawMiniStatement() {
        Bank bank = new Bank();
        // 1 (create) -> type "CURRENT", name "Bob", opening 2000
        // 2 (deposit) -> "CA2001", 500
        // 3 (withdraw) -> "CA2001", 1000
        // 6 (mini statement) -> "CA2001"
        // 0 (exit)
        String inputs = String.join(System.lineSeparator(),
                "1", "2", "Bob", "2000",
                "2", "CA2001", "500",
                "3", "CA2001", "1000",
                "6", "CA2001",
                "0"
        ) + System.lineSeparator();

        String output = runBankAppWithInputs(inputs, bank);

        assertTrue(output.contains("Deposited 500.00 into CA2001"));
        assertTrue(output.contains("Withdrew 1000.00 from CA2001"));
        assertTrue(output.contains("Mini Statement for CA2001"));
        assertTrue(output.contains("DEPOSIT"));
        assertTrue(output.contains("WITHDRAW"));
    }

    @Test
    @DisplayName("Menu action 4 transfers funds between accounts with custom transaction types")
    void testTransferAction() {
        Bank bank = new Bank();
        // 1 -> SAVINGS "Alice" 3000 -> SB1001
        // 1 -> CURRENT "Bob" 1000 -> CA2001
        // 4 (transfer) -> "SB1001", "CA2001", 1200
        // 0 (exit)
        String inputs = String.join(System.lineSeparator(),
                "1", "1", "Alice", "3000",
                "1", "2", "Bob", "1000",
                "4", "SB1001", "CA2001", "1200",
                "0"
        ) + System.lineSeparator();

        String output = runBankAppWithInputs(inputs, bank);

        assertTrue(output.contains("Transferred 1200.00 from SB1001 to CA2001"));
        assertEquals(1800, bank.getAccounts().get("SB1001").getBalance());
        assertEquals(2200, bank.getAccounts().get("CA2001").getBalance());
    }

    @Test
    @DisplayName("Menu action 7 validates that accounts with non-zero balance cannot be closed")
    void testCloseAccountAction() {
        Bank bank = new Bank();
        // 1 -> CURRENT "Temp" 500 -> CA2001
        // 7 -> close "CA2001" -> fails
        // 3 -> withdraw "CA2001", 500 -> balance becomes 0
        // 7 -> close "CA2001" -> succeeds
        // 0 -> exit
        String inputs = String.join(System.lineSeparator(),
                "1", "2", "Temp", "500",
                "7", "CA2001",
                "3", "CA2001", "500",
                "7", "CA2001",
                "0"
        ) + System.lineSeparator();

        String output = runBankAppWithInputs(inputs, bank);

        assertTrue(output.contains("Cannot close account CA2001: Balance must be 0.0"));
        assertTrue(output.contains("Account CA2001 has been closed successfully."));
    }

    @Test
    @DisplayName("BankApp never crashes on invalid inputs and handles all target exceptions")
    void testAppNeverCrashesOnBadInput() {
        Bank bank = new Bank();

        // 1. Invalid menu choice (text "abc" -> InputMismatchException)
        // 2. Out of range menu choice (99)
        // 3. Create account with non-numeric balance (text "fifty" -> InputMismatchException)
        // 4. Create account with unsupported type -> IllegalArgumentException
        // 5. Create savings account with opening balance < 500 (300) -> IllegalArgumentException
        // 6. Deposit to unknown account -> AccountNotFoundException
        // 7. Withdraw exceeding funds on savings -> InsufficientBalanceException
        // 8. Transfer to same account -> IllegalArgumentException
        // 9. Exit (0)
        String inputs = String.join(System.lineSeparator(),
                "abc",
                "99",
                "1", "1", "TestUser", "invalid_amount",
                "1", "INVESTMENT", "TestUser", "1000",
                "1", "1", "TestUser", "300",
                "1", "1", "ValidUser", "1000",      // creates SB1001
                "2", "UNKNOWN_ACC", "500",
                "3", "SB1001", "50000",
                "4", "SB1001", "SB1001", "100",
                "0"
        ) + System.lineSeparator();

        assertDoesNotThrow(() -> {
            String output = runBankAppWithInputs(inputs, bank);
            assertTrue(output.contains("Invalid choice! Please enter a numeric value"));
            assertTrue(output.contains("Invalid option (99)"));
            assertTrue(output.contains("Invalid input format: Expected a numeric value"));
            assertTrue(output.contains("Unsupported account type"));
            assertTrue(output.contains("Savings account requires an opening balance of at least 500"));
            assertTrue(output.contains("Account Not Found"));
            assertTrue(output.contains("Insufficient Balance"));
            assertTrue(output.contains("Cannot transfer to the same account"));
            assertTrue(output.contains("Goodbye!"));
        });
    }
}
