package com.afroz.bank;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class DBConnectionTest {

    @Test
    @DisplayName("DBConnection specifies the correct environment variable names")
    void testEnvironmentVariableNames() {
        assertEquals("DB_URL", DBConnection.ENV_DB_URL);
        assertEquals("DB_USER", DBConnection.ENV_DB_USER);
        assertEquals("DB_PASSWORD", DBConnection.ENV_DB_PASSWORD);
    }

    @Test
    @DisplayName("getConnection throws SQLException when credentials are not configured in environment")
    void testGetConnectionThrowsWhenUnconfigured() {
        if (!DBConnection.isConfigured()) {
            SQLException ex = assertThrows(SQLException.class, DBConnection::getConnection);
            assertTrue(ex.getMessage().contains("DB_URL"));
            assertTrue(ex.getMessage().contains("DB_USER"));
            assertTrue(ex.getMessage().contains("DB_PASSWORD"));
        }
    }
}

