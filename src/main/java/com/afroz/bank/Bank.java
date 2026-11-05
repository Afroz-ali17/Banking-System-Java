package com.afroz.bank;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages bank accounts using a HashMap for fast O(1) lookups by account number.
 * Supports persistent storage via AccountDAO and MySQL JDBC transactions.
 */
public class Bank {
    // HashMap chosen for O(1) average-time lookups, insertions, and deletions by account number
    private final HashMap<String, Account> accounts = new HashMap<>();
    private final AccountDAO accountDAO;

    // Sequential counters for generating account numbers
    private int nextSavingsNumber = 1001;
    private int nextCurrentNumber = 2001;

    public Bank() {
        this(new AccountDAO());
    }

    public Bank(AccountDAO accountDAO) {
        this.accountDAO = accountDAO;
        loadAccountsFromDatabase();
    }

    /**
     * Loads existing accounts from the database on startup if database is configured.
     * Also updates next account number counters to prevent ID collisions.
     */
    public synchronized void loadAccountsFromDatabase() {
        if (accountDAO != null && DBConnection.isConfigured()) {
            try {
                List<Account> loadedAccounts = accountDAO.findAll();
                for (Account account : loadedAccounts) {
                    accounts.put(account.getAccountNumber(), account);
                    updateNextAccountCounters(account.getAccountNumber());
                }
                if (!loadedAccounts.isEmpty()) {
                    System.out.println("[Database] Successfully loaded " + loadedAccounts.size() + " accounts on startup.");
                }
            } catch (SQLException e) {
                System.err.println("[Database Warning] Could not load accounts from database: " + e.getMessage());
            }
        }
    }

    private void updateNextAccountCounters(String accNo) {
        if (accNo == null) return;
        if (accNo.startsWith("SB")) {
            try {
                int num = Integer.parseInt(accNo.substring(2));
                if (num >= nextSavingsNumber) {
                    nextSavingsNumber = num + 1;
                }
            } catch (NumberFormatException ignored) {}
        } else if (accNo.startsWith("CA")) {
            try {
                int num = Integer.parseInt(accNo.substring(2));
                if (num >= nextCurrentNumber) {
                    nextCurrentNumber = num + 1;
                }
            } catch (NumberFormatException ignored) {}
        }
    }

    /**
     * Creates and registers a new account.
     * Generates numbers like SB1001 for Savings and CA2001 for Current accounts.
     * Rejects Savings accounts with an opening balance below 500.
     * Persists the account to the database if configured.
     *
     * @param type    the account type ("SAVINGS" or "CURRENT")
     * @param name    the account holder's name
     * @param opening the initial deposit / opening balance
     * @return the newly created Account
     * @throws IllegalArgumentException if the account type is invalid or opening balance is unacceptable
     */
    public synchronized Account createAccount(String type, String name, double opening) {
        if (type == null || type.trim().isEmpty()) {
            throw new IllegalArgumentException("Account type cannot be null or empty");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Account holder name cannot be null or empty");
        }

        String normalizedType = type.trim().toUpperCase();
        Account account;

        if (normalizedType.equals("SAVINGS") || normalizedType.equals("SAVING") || normalizedType.equals("SB")) {
            if (opening < 500) {
                throw new IllegalArgumentException("Savings account requires an opening balance of at least 500. Provided: " + opening);
            }
            String accNo = "SB" + nextSavingsNumber++;
            account = new SavingsAccount(accNo, name.trim(), opening);
        } else if (normalizedType.equals("CURRENT") || normalizedType.equals("CA")) {
            if (opening < 0) {
                throw new IllegalArgumentException("Current account opening balance cannot be negative. Provided: " + opening);
            }
            String accNo = "CA" + nextCurrentNumber++;
            account = new CurrentAccount(accNo, name.trim(), opening);
        } else {
            throw new IllegalArgumentException("Unsupported account type: '" + type + "'. Supported types: SAVINGS, CURRENT");
        }

        accounts.put(account.getAccountNumber(), account);

        // Persist to database if configured
        if (accountDAO != null && DBConnection.isConfigured()) {
            try {
                accountDAO.save(account);
                accountDAO.addTransaction(account.getAccountNumber(), "DEPOSIT", opening);
            } catch (SQLException e) {
                System.err.println("[Database Warning] Could not persist created account: " + e.getMessage());
            }
        }

        return account;
    }

    /**
     * Looks up an account by its unique account number.
     *
     * @param accNo the account number to search for
     * @return the matching Account
     * @throws AccountNotFoundException if no account exists with the given account number
     */
    public synchronized Account getAccount(String accNo) throws AccountNotFoundException {
        if (accNo == null) {
            throw new AccountNotFoundException("Account number cannot be null");
        }
        Account account = accounts.get(accNo);
        if (account == null) {
            throw new AccountNotFoundException("Account not found with account number: " + accNo);
        }
        return account;
    }

    /**
     * Deposits funds into an account and updates database persistence if configured.
     *
     * @param accNo  the account number
     * @param amount the deposit amount
     * @throws AccountNotFoundException if the account does not exist
     */
    public synchronized void deposit(String accNo, double amount) throws AccountNotFoundException {
        Account account = getAccount(accNo);
        account.deposit(amount);
        if (accountDAO != null && DBConnection.isConfigured()) {
            try {
                accountDAO.updateBalance(accNo, account.getBalance());
                accountDAO.addTransaction(accNo, "DEPOSIT", amount);
            } catch (SQLException e) {
                System.err.println("[Database Warning] Could not persist deposit: " + e.getMessage());
            }
        }
    }

    /**
     * Withdraws funds from an account and updates database persistence if configured.
     *
     * @param accNo  the account number
     * @param amount the withdrawal amount
     * @throws AccountNotFoundException     if the account does not exist
     * @throws InsufficientBalanceException if withdrawal breaches balance rules
     */
    public synchronized void withdraw(String accNo, double amount)
            throws AccountNotFoundException, InsufficientBalanceException {
        Account account = getAccount(accNo);
        account.withdraw(amount);
        if (accountDAO != null && DBConnection.isConfigured()) {
            try {
                accountDAO.updateBalance(accNo, account.getBalance());
                accountDAO.addTransaction(accNo, "WITHDRAW", amount);
            } catch (SQLException e) {
                System.err.println("[Database Warning] Could not persist withdrawal: " + e.getMessage());
            }
        }
    }

    /**
     * Closes an existing account. The operation only succeeds if the account balance is 0.
     * Deletes the account from the database if configured.
     *
     * @param accNo the account number to close
     * @return true if the account was successfully closed, false if balance is non-zero
     * @throws AccountNotFoundException if the account number does not exist
     */
    public synchronized boolean closeAccount(String accNo) throws AccountNotFoundException {
        Account account = getAccount(accNo);
        if (Double.compare(account.getBalance(), 0.0) != 0) {
            System.out.println("Cannot close account " + accNo + ": balance must be 0 (current balance: " + account.getBalance() + ")");
            return false;
        }
        accounts.remove(accNo);

        // Delete from database if configured
        if (accountDAO != null && DBConnection.isConfigured()) {
            try {
                accountDAO.delete(accNo);
            } catch (SQLException e) {
                System.err.println("[Database Warning] Could not delete closed account from DB: " + e.getMessage());
            }
        }

        return true;
    }

    /**
     * Transfers funds from one account to another in a thread-safe, deadlock-free manner.
     * If database persistence is configured, uses a real JDBC transaction:
     * - setAutoCommit(false)
     * - both balance updates and transaction logs executed under the transaction
     * - commit() on success
     * - rollback() on any exception
     *
     * @param fromAcc source account number
     * @param toAcc   target account number
     * @param amount  amount to transfer
     * @throws AccountNotFoundException     if either source or target account is not found
     * @throws InsufficientBalanceException if source account lacks required balance/overdraft
     * @throws IllegalArgumentException     if amount <= 0 or source and target accounts are the same
     */
    public void transfer(String fromAcc, String toAcc, double amount)
            throws AccountNotFoundException, InsufficientBalanceException {
        if (fromAcc == null || toAcc == null) {
            throw new IllegalArgumentException("Source and target account numbers cannot be null");
        }
        if (fromAcc.trim().equalsIgnoreCase(toAcc.trim())) {
            throw new IllegalArgumentException("Cannot transfer to the same account: " + fromAcc);
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Transfer amount must be positive. Provided: " + amount);
        }

        Account source = getAccount(fromAcc.trim());
        Account target = getAccount(toAcc.trim());

        // Deadlock prevention: Lock the two accounts in a globally consistent order (by account number)
        Account firstLock = source.getAccountNumber().compareTo(target.getAccountNumber()) < 0 ? source : target;
        Account secondLock = source.getAccountNumber().compareTo(target.getAccountNumber()) < 0 ? target : source;

        synchronized (firstLock) {
            synchronized (secondLock) {
                if (accountDAO != null && DBConnection.isConfigured()) {
                    Connection conn = null;
                    boolean committed = false;
                    try {
                        conn = DBConnection.getConnection();
                        conn.setAutoCommit(false); // 1. Start JDBC Transaction

                        // Withdraw from source (in-memory validation)
                        source.transferOut(amount);

                        // 2. Perform both balance updates and transaction inserts in DB
                        accountDAO.updateBalance(conn, source.getAccountNumber(), source.getBalance());
                        accountDAO.addTransaction(conn, source.getAccountNumber(), "TRANSFER_OUT", amount);

                        double newTargetBalance = target.getBalance() + amount;
                        accountDAO.updateBalance(conn, target.getAccountNumber(), newTargetBalance);
                        accountDAO.addTransaction(conn, target.getAccountNumber(), "TRANSFER_IN", amount);

                        // Apply in-memory target deposit
                        target.transferIn(amount);

                        // 3. Commit the transaction
                        conn.commit();
                        committed = true;
                    } catch (Exception e) {
                        if (conn != null && !committed) {
                            try {
                                conn.rollback(); // 4. Rollback on any exception
                            } catch (SQLException rollbackEx) {
                                System.err.println("[Database Error] Transaction rollback failed: " + rollbackEx.getMessage());
                            }
                        }
                        // Manual in-memory rollback if source had already been deducted
                        source.restoreBalance(amount);

                        if (e instanceof InsufficientBalanceException) {
                            throw (InsufficientBalanceException) e;
                        } else if (e instanceof IllegalArgumentException) {
                            throw (IllegalArgumentException) e;
                        } else {
                            throw new RuntimeException("Transfer transaction failed: " + e.getMessage(), e);
                        }
                    } finally {
                        if (conn != null) {
                            try {
                                conn.setAutoCommit(true);
                                conn.close();
                            } catch (SQLException ignored) {}
                        }
                    }
                } else {
                    // In-memory fallback (when running without an active database)
                    source.transferOut(amount);
                    try {
                        target.transferIn(amount);
                    } catch (Exception e) {
                        source.restoreBalance(amount);
                        throw e;
                    }
                }
            }
        }
    }

    /**
     * Lists all registered accounts to standard output and returns them as a List.
     *
     * @return a List containing all registered accounts
     */
    public synchronized List<Account> listAccounts() {
        List<Account> accountList = new ArrayList<>(accounts.values());
        if (accountList.isEmpty()) {
            System.out.println("No accounts currently registered in the bank.");
        } else {
            System.out.println("=== Bank Accounts List (" + accountList.size() + " total) ===");
            for (Account acc : accountList) {
                System.out.println(acc);
            }
        }
        return accountList;
    }

    /**
     * Returns an unmodifiable view of all registered accounts.
     *
     * @return unmodifiable map of account numbers to Account instances
     */
    public synchronized Map<String, Account> getAccounts() {
        return Collections.unmodifiableMap(accounts);
    }

    /**
     * Returns the total number of accounts registered in the bank.
     *
     * @return count of registered accounts
     */
    public synchronized int getAccountCount() {
        return accounts.size();
    }
}
