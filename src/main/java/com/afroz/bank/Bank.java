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

