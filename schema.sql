-- =============================================================================
-- Banking System Database Schema (Day 6: MySQL Persistence via JDBC)
-- =============================================================================

CREATE DATABASE IF NOT EXISTS bank_db;
USE bank_db;

-- 1. Accounts Table
CREATE TABLE IF NOT EXISTS accounts (
    account_number VARCHAR(20) PRIMARY KEY,
    holder_name VARCHAR(100) NOT NULL,
    type VARCHAR(20) NOT NULL,
    balance DECIMAL(15, 2) NOT NULL
);

-- 2. Transactions Table
CREATE TABLE IF NOT EXISTS transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    account_number VARCHAR(20) NOT NULL,
    type VARCHAR(20) NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_account_number FOREIGN KEY (account_number)
        REFERENCES accounts(account_number)
        ON DELETE CASCADE
);

-- Index on account_number for fast transaction lookups
CREATE INDEX idx_transactions_account ON transactions(account_number);

