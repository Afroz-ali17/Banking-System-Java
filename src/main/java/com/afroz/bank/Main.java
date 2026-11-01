package com.afroz.bank;

public class Main {
    public static void main(String[] args) {
        Bank bank = new Bank();

        System.out.println("=== 1. Creating 3 Accounts through Bank ===");
        Account a1 = bank.createAccount("SAVINGS", "Afroz", 10000);
        Account a2 = bank.createAccount("CURRENT", "Ali", 2000);
        Account a3 = bank.createAccount("SAVINGS", "Sara", 5000);

        System.out.println("Created: " + a1);
        System.out.println("Created: " + a2);
        System.out.println("Created: " + a3);

        System.out.println("\n=== 2. Performing Sample Transactions ===");
        try {
            a1.deposit(2500);
            a1.withdraw(3000);
            a2.withdraw(6000); // allowed: uses overdraft
        } catch (InsufficientBalanceException e) {
            System.out.println("Transaction failed: " + e.getMessage());
        }

        System.out.println("\n=== 3. Listing All Accounts in Bank ===");
        bank.listAccounts();

        System.out.println("\n=== 4. Looking up an Existing Account ===");
        try {
            Account lookedUp = bank.getAccount("SB1001");
            System.out.println("Lookup result: " + lookedUp);
            System.out.println("Transaction history for " + lookedUp.getAccountNumber() + ":");
            lookedUp.getHistory().forEach(System.out::println);
        } catch (AccountNotFoundException e) {
            System.out.println("Lookup failed: " + e.getMessage());
        }

        System.out.println("\n=== 5. Triggering AccountNotFoundException ===");
        try {
            System.out.println("Attempting to lookup non-existent account 'SB9999'...");
            bank.getAccount("SB9999");
        } catch (AccountNotFoundException e) {
            System.out.println("Caught expected AccountNotFoundException: " + e.getMessage());
        }

        System.out.println("\n=== 6. Validating Savings Opening Balance Rule (< 500) ===");
        try {
            System.out.println("Attempting to create Savings Account with opening balance 300...");
            bank.createAccount("SAVINGS", "Invalid Savings", 300);
        } catch (IllegalArgumentException e) {
            System.out.println("Rejected as expected: " + e.getMessage());
        }

        System.out.println("\n=== 7. Validating Account Closure Rule (Only when balance is 0) ===");
        try {
            System.out.println("Attempting to close " + a3.getAccountNumber() + " with active balance: " + a3.getBalance());
            boolean closedNonZero = bank.closeAccount(a3.getAccountNumber());
            System.out.println("Closed non-zero balance account? " + closedNonZero);

            Account tempCurrent = bank.createAccount("CURRENT", "Temp Account", 0);
            System.out.println("Created zero-balance account: " + tempCurrent);
            boolean closedZero = bank.closeAccount(tempCurrent.getAccountNumber());
            System.out.println("Closed zero-balance account? " + closedZero);
        } catch (AccountNotFoundException e) {
            System.out.println("Error closing account: " + e.getMessage());
        }
    }
}
