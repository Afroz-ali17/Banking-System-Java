package com.afroz.bank;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Manages JDBC connections to MySQL.
 * Reads credentials strictly from environment variables (DB_URL, DB_USER, DB_PASSWORD).
 * Never hardcodes database credentials.
 */
public class DBConnection {

    public static final String ENV_DB_URL = "DB_URL";
    public static final String ENV_DB_USER = "DB_USER";
    public static final String ENV_DB_PASSWORD = "DB_PASSWORD";

    private DBConnection() {}

    /**
     * Checks if the required database environment variables are configured.
     *
     * @return true if DB_URL, DB_USER, and DB_PASSWORD are all non-null and set
     */
    public static boolean isConfigured() {
        String url = System.getenv(ENV_DB_URL);
        String user = System.getenv(ENV_DB_USER);
        String password = System.getenv(ENV_DB_PASSWORD);
        return url != null && !url.trim().isEmpty()
                && user != null && !user.trim().isEmpty()
                && password != null;
    }

    /**
     * Creates and returns a new JDBC database connection.
     *
     * @return active java.sql.Connection
     * @throws SQLException if environment variables are missing or connection fails
     */
    public static Connection getConnection() throws SQLException {
        String url = System.getenv(ENV_DB_URL);
        String user = System.getenv(ENV_DB_USER);
        String password = System.getenv(ENV_DB_PASSWORD);

        if (url == null || url.trim().isEmpty() || user == null || user.trim().isEmpty() || password == null) {
            throw new SQLException("Database credentials not configured. Please set environment variables: "
                    + ENV_DB_URL + ", " + ENV_DB_USER + ", and " + ENV_DB_PASSWORD);
        }

        return DriverManager.getConnection(url.trim(), user.trim(), password);
    }
}

