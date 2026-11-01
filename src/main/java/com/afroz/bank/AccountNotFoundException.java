package com.afroz.bank;

/**
 * Checked exception thrown when an account lookup by account number fails.
 * Being a checked exception, callers are required to handle or declare it explicitly.
 */
public class AccountNotFoundException extends Exception {
    public AccountNotFoundException(String message) {
        super(message);
    }

    public AccountNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

