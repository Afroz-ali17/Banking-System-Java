package com.afroz.bank;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages bank accounts using a HashMap for fast O(1) lookups by account number.
 */
public class Bank {
    // HashMap chosen for O(1) average-time lookups, insertions, and deletions by account number
    private final HashMap<String, Account> accounts = new HashMap<>();

    // Sequential counters for generating account numbers
    private int nextSavingsNumber = 1001;
    private int nextCurrentNumber = 2001;

    /**
     * Creates and registers a new account.
     * Generates numbers like SB1001 for Savings and CA2001 for Current accounts.
     * Rejects Savings accounts with an opening balance below 500.
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
     * Closes an existing account. The operation only succeeds if the account balance is 0.
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
        return true;
    }

    /**
     * Transfers funds from one account to another.
     * Validates that both accounts exist, the amount is positive, and the accounts differ.
     * Withdraws from the source account as TRANSFER_OUT, then deposits into the target as TRANSFER_IN.
     * If the deposit fails after the withdrawal, restores the source balance (manual rollback).
     *
     * @param fromAcc source account number
     * @param toAcc   target account number
     * @param amount  amount to transfer
     * @throws AccountNotFoundException     if either source or target account is not found
     * @throws InsufficientBalanceException if source account lacks required balance/overdraft
     * @throws IllegalArgumentException     if amount <= 0 or source and target accounts are the same
     */
    public synchronized void transfer(String fromAcc, String toAcc, double amount)
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

        // Step 1: Withdraw from source (recorded as TRANSFER_OUT)
        source.transferOut(amount);

        // Step 2: Deposit into target (recorded as TRANSFER_IN), with manual rollback on deposit failure
        try {
            target.transferIn(amount);
        } catch (Exception e) {
            source.restoreBalance(amount);
            throw e;
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
