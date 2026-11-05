package com.afroz.bank;

import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

/**
 * Menu-driven console UI for the Banking System.
 * Main entry point for user interaction.
 */
public class BankApp {

    @FunctionalInterface
    public interface BankAction {
        void execute() throws AccountNotFoundException, InsufficientBalanceException;
    }

    private final Bank bank;
    private final Scanner scanner;

    public BankApp(Bank bank, Scanner scanner) {
        this.bank = bank;
        this.scanner = scanner;
    }

    public static void main(String[] args) {
        Bank bank = new Bank();
        Scanner scanner = new Scanner(System.in);
        BankApp app = new BankApp(bank, scanner);
        app.start();
    }

    /**
     * Starts the main menu loop.
     */
    public void start() {
        boolean running = true;
        System.out.println("==================================================");
        System.out.println("          WELCOME TO AFROZ BANKING SYSTEM         ");
        System.out.println("==================================================");

        while (running) {
            printMenu();
            int choice = -1;
            try {
                System.out.print("Enter your choice (0-7): ");
                choice = scanner.nextInt();
                scanner.nextLine(); // consume newline
            } catch (InputMismatchException e) {
                System.out.println("\n[Error] Invalid choice! Please enter a numeric value between 0 and 7.");
                scanner.nextLine(); // discard the invalid token to avoid infinite loop
                continue;
            }

            switch (choice) {
                case 1 -> createAccountAction();
                case 2 -> depositAction();
                case 3 -> withdrawAction();
                case 4 -> transferAction();
                case 5 -> checkBalanceAction();
                case 6 -> miniStatementAction();
                case 7 -> closeAccountAction();
                case 0 -> {
                    System.out.println("\nThank you for banking with us. Goodbye!");
                    running = false;
                }
                default -> System.out.println("\n[Error] Invalid option (" + choice + "). Please choose between 0 and 7.");
            }
        }
    }

    private void printMenu() {
        System.out.println("\n--------------------------------------------------");
        System.out.println("                   MAIN MENU                      ");
        System.out.println("--------------------------------------------------");
        System.out.println("  1. Create Account");
        System.out.println("  2. Deposit");
        System.out.println("  3. Withdraw");
        System.out.println("  4. Transfer");
        System.out.println("  5. Check Balance");
        System.out.println("  6. Mini Statement");
        System.out.println("  7. Close Account");
        System.out.println("  0. Exit");
        System.out.println("--------------------------------------------------");
    }

    /**
     * Executes an action wrapped in try/catch for the 4 target exceptions:
     * InsufficientBalanceException, AccountNotFoundException,
     * IllegalArgumentException, and InputMismatchException.
     */
    private void executeAction(String actionName, BankAction action) {
        try {
            action.execute();
        } catch (InputMismatchException e) {
            System.out.println("\n[Error] Invalid input format: Expected a numeric value.");
            scanner.nextLine(); // clear offending token to prevent scanner loop
        } catch (InsufficientBalanceException e) {
            System.out.println("\n[Error] Insufficient Balance: " + e.getMessage());
        } catch (AccountNotFoundException e) {
            System.out.println("\n[Error] Account Not Found: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("\n[Error] Invalid Input: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("\n[Error] An unexpected error occurred: " + e.getMessage());
        }
    }

    // 1. Create Account
    public void createAccountAction() {
        executeAction("Create Account", () -> {
            System.out.println("\n--- [1] Create New Account ---");
            System.out.print("Enter Account Type (1 for SAVINGS, 2 for CURRENT): ");
            String typeInput = scanner.nextLine().trim();
            String type;
            if ("1".equals(typeInput)) {
                type = "SAVINGS";
            } else if ("2".equals(typeInput)) {
                type = "CURRENT";
            } else {
                type = typeInput;
            }

            System.out.print("Enter Account Holder Name: ");
            String name = scanner.nextLine().trim();

            System.out.print("Enter Opening Balance: ");
            double opening = scanner.nextDouble();
            scanner.nextLine(); // consume newline

            Account account = bank.createAccount(type, name, opening);
            System.out.println("\n[Success] Account created successfully!");
            System.out.println("  Account Number : " + account.getAccountNumber());
            System.out.println("  Holder Name    : " + account.getHolderName());
            System.out.printf("  Current Balance: %.2f%n", account.getBalance());
        });
    }

    // 2. Deposit
    public void depositAction() {
        executeAction("Deposit", () -> {
            System.out.println("\n--- [2] Deposit Funds ---");
            System.out.print("Enter Account Number: ");
            String accNo = scanner.nextLine().trim();

            System.out.print("Enter Deposit Amount: ");
            double amount = scanner.nextDouble();
            scanner.nextLine(); // consume newline

            bank.deposit(accNo, amount);
            Account account = bank.getAccount(accNo);
            System.out.printf("\n[Success] Deposited %.2f into %s. New Balance: %.2f%n",
                    amount, accNo, account.getBalance());
        });
    }

    // 3. Withdraw
    public void withdrawAction() {
        executeAction("Withdraw", () -> {
            System.out.println("\n--- [3] Withdraw Funds ---");
            System.out.print("Enter Account Number: ");
            String accNo = scanner.nextLine().trim();

            System.out.print("Enter Withdrawal Amount: ");
            double amount = scanner.nextDouble();
            scanner.nextLine(); // consume newline

            bank.withdraw(accNo, amount);
            Account account = bank.getAccount(accNo);
            System.out.printf("\n[Success] Withdrew %.2f from %s. New Balance: %.2f%n",
                    amount, accNo, account.getBalance());
        });
    }

    // 4. Transfer
    public void transferAction() {
        executeAction("Transfer", () -> {
            System.out.println("\n--- [4] Transfer Funds ---");
            System.out.print("Enter Source Account Number: ");
            String fromAcc = scanner.nextLine().trim();

            System.out.print("Enter Target Account Number: ");
            String toAcc = scanner.nextLine().trim();

            System.out.print("Enter Transfer Amount: ");
            double amount = scanner.nextDouble();
            scanner.nextLine(); // consume newline

            bank.transfer(fromAcc, toAcc, amount);
            Account source = bank.getAccount(fromAcc);
            Account target = bank.getAccount(toAcc);
            System.out.printf("\n[Success] Transferred %.2f from %s to %s.%n", amount, fromAcc, toAcc);
            System.out.printf("  Source Balance (%s): %.2f%n", fromAcc, source.getBalance());
            System.out.printf("  Target Balance (%s): %.2f%n", toAcc, target.getBalance());
        });
    }

    // 5. Check Balance
    public void checkBalanceAction() {
        executeAction("Check Balance", () -> {
            System.out.println("\n--- [5] Check Account Balance ---");
            System.out.print("Enter Account Number: ");
            String accNo = scanner.nextLine().trim();

            Account account = bank.getAccount(accNo);
            System.out.println("\n[Account Details]");
            System.out.println("  Account Number : " + account.getAccountNumber());
            System.out.println("  Holder Name    : " + account.getHolderName());
            System.out.println("  Account Type   : " + account.getClass().getSimpleName());
            System.out.printf("  Current Balance: %.2f%n", account.getBalance());
        });
    }

    // 6. Mini Statement
    public void miniStatementAction() {
        executeAction("Mini Statement", () -> {
            System.out.println("\n--- [6] Mini Statement ---");
            System.out.print("Enter Account Number: ");
            String accNo = scanner.nextLine().trim();

            Account account = bank.getAccount(accNo);
            System.out.println("\n[Mini Statement for " + account.getAccountNumber() + " (" + account.getHolderName() + ")]");
            System.out.printf("Current Balance: %.2f%n", account.getBalance());
            System.out.println("Transaction History:");
            List<Transaction> history = account.getHistory();
            if (history.isEmpty()) {
                System.out.println("  (No transactions recorded yet)");
            } else {
                for (int i = 0; i < history.size(); i++) {
                    System.out.printf("  %2d. %s%n", (i + 1), history.get(i));
                }
            }
        });
    }

    // 7. Close Account
    public void closeAccountAction() {
        executeAction("Close Account", () -> {
            System.out.println("\n--- [7] Close Account ---");
            System.out.print("Enter Account Number to Close: ");
            String accNo = scanner.nextLine().trim();

            boolean closed = bank.closeAccount(accNo);
            if (closed) {
                System.out.println("\n[Success] Account " + accNo + " has been closed successfully.");
            } else {
                Account account = bank.getAccount(accNo);
                System.out.printf("\n[Error] Cannot close account %s: Balance must be 0.0 (Current: %.2f).%n",
                        accNo, account.getBalance());
            }
        });
    }
}
