package com.afroz.bank;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) for Account and Transaction persistence.
 * Uses PreparedStatement exclusively to prevent SQL injection vulnerabilities.
 */
public class AccountDAO {

    /**
     * Saves a new account or updates an existing one in the database.
     *
     * @param account the account to save
     * @throws SQLException if a database access error occurs
     */
    public void save(Account account) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            save(conn, account);
        }
    }

    /**
     * Saves an account using an existing database connection (e.g., inside an active transaction).
     */
    public void save(Connection conn, Account account) throws SQLException {
        String sql = "INSERT INTO accounts (account_number, holder_name, type, balance) VALUES (?, ?, ?, ?) "
                   + "ON DUPLICATE KEY UPDATE holder_name = VALUES(holder_name), balance = VALUES(balance)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String type = (account instanceof SavingsAccount) ? "SAVINGS" : "CURRENT";
            pstmt.setString(1, account.getAccountNumber());
            pstmt.setString(2, account.getHolderName());
            pstmt.setString(3, type);
            pstmt.setBigDecimal(4, BigDecimal.valueOf(account.getBalance()));
            pstmt.executeUpdate();
        }
    }

    /**
     * Finds an account by its unique account number.
     *
     * @param accountNumber the account number to search for
     * @return the loaded Account with historical transactions, or null if not found
     * @throws SQLException if a database access error occurs
     */
    public Account findByNumber(String accountNumber) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            return findByNumber(conn, accountNumber);
        }
    }

    /**
     * Finds an account using an existing connection.
     */
    public Account findByNumber(Connection conn, String accountNumber) throws SQLException {
        String sql = "SELECT account_number, holder_name, type, balance FROM accounts WHERE account_number = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, accountNumber);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String accNo = rs.getString("account_number");
                    String name = rs.getString("holder_name");
                    String type = rs.getString("type");
                    double balance = rs.getBigDecimal("balance").doubleValue();

                    Account account = "SAVINGS".equalsIgnoreCase(type)
                            ? new SavingsAccount(accNo, name, balance)
                            : new CurrentAccount(accNo, name, balance);

                    loadTransactionsForAccount(conn, account);
                    return account;
                }
            }
        }
        return null;
    }

    /**
     * Updates the balance of an account.
     *
     * @param accountNumber the account number
     * @param newBalance    the new balance
     * @throws SQLException if a database access error occurs
     */
    public void updateBalance(String accountNumber, double newBalance) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            updateBalance(conn, accountNumber, newBalance);
        }
    }

    /**
     * Updates the balance of an account within a shared connection/transaction.
     */
    public void updateBalance(Connection conn, String accountNumber, double newBalance) throws SQLException {
        String sql = "UPDATE accounts SET balance = ? WHERE account_number = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setBigDecimal(1, BigDecimal.valueOf(newBalance));
            pstmt.setString(2, accountNumber);
            pstmt.executeUpdate();
        }
    }

    /**
     * Records a new transaction in the transactions table.
     *
     * @param accountNumber the account number
     * @param type          transaction type (DEPOSIT, WITHDRAW, TRANSFER_IN, TRANSFER_OUT)
     * @param amount        transaction amount
     * @throws SQLException if a database access error occurs
     */
    public void addTransaction(String accountNumber, String type, double amount) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            addTransaction(conn, accountNumber, type, amount);
        }
    }

    /**
     * Records a new transaction within a shared connection/transaction.
     */
    public void addTransaction(Connection conn, String accountNumber, String type, double amount) throws SQLException {
        String sql = "INSERT INTO transactions (account_number, type, amount, created_at) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, accountNumber);
            pstmt.setString(2, type);
            pstmt.setBigDecimal(3, BigDecimal.valueOf(amount));
            pstmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.executeUpdate();
        }
    }

    /**
     * Loads all accounts from the database, including their transaction histories.
     *
     * @return List of all accounts in the database
     * @throws SQLException if a database access error occurs
     */
    public List<Account> findAll() throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            return findAll(conn);
        }
    }

    /**
     * Loads all accounts using an existing connection.
     */
    public List<Account> findAll(Connection conn) throws SQLException {
        List<Account> accounts = new ArrayList<>();
        String sql = "SELECT account_number, holder_name, type, balance FROM accounts ORDER BY account_number ASC";
        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                String accNo = rs.getString("account_number");
                String name = rs.getString("holder_name");
                String type = rs.getString("type");
                double balance = rs.getBigDecimal("balance").doubleValue();

                Account account = "SAVINGS".equalsIgnoreCase(type)
                        ? new SavingsAccount(accNo, name, balance)
                        : new CurrentAccount(accNo, name, balance);

                loadTransactionsForAccount(conn, account);
                accounts.add(account);
            }
        }
        return accounts;
    }

    /**
     * Deletes an account from the database. Cascade constraints remove its transactions.
     *
     * @param accountNumber the account number to delete
     * @throws SQLException if a database access error occurs
     */
    public void delete(String accountNumber) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            delete(conn, accountNumber);
        }
    }

    /**
     * Deletes an account within a shared connection/transaction.
     */
    public void delete(Connection conn, String accountNumber) throws SQLException {
        String sql = "DELETE FROM accounts WHERE account_number = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, accountNumber);
            pstmt.executeUpdate();
        }
    }

    /**
     * Helper to load transaction history for a given account.
     */
    private void loadTransactionsForAccount(Connection conn, Account account) throws SQLException {
        String sql = "SELECT type, amount, created_at FROM transactions WHERE account_number = ? ORDER BY created_at ASC, id ASC";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, account.getAccountNumber());
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String type = rs.getString("type");
                    double amount = rs.getBigDecimal("amount").doubleValue();
                    Timestamp ts = rs.getTimestamp("created_at");
                    LocalDateTime time = (ts != null) ? ts.toLocalDateTime() : LocalDateTime.now();
                    account.addHistoricalTransaction(new Transaction(type, amount, time));
                }
            }
        }
    }
}

