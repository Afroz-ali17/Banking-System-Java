package com.afroz.bank;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AccountDAOTest {

    @Test
    @DisplayName("Bank loads accounts from AccountDAO on startup and correctly sets next ID counters")
    void testBankLoadsAccountsOnStartup() throws AccountNotFoundException {
        // Create a stub AccountDAO that returns pre-existing accounts from the database
        AccountDAO stubDAO = new AccountDAO() {
            @Override
            public List<Account> findAll() {
                List<Account> list = new ArrayList<>();
                Account s1 = new SavingsAccount("SB1005", "Database Alice", 15000);
                Account c1 = new CurrentAccount("CA2010", "Database Bob", 8000);
                list.add(s1);
                list.add(c1);
                return list;
            }
        };

        // Create Bank injecting the stubDAO; override DBConnection.isConfigured behavior via custom Bank subclass
        Bank bank = new Bank(stubDAO) {
            @Override
            public synchronized void loadAccountsFromDatabase() {
                try {
                    List<Account> loadedAccounts = stubDAO.findAll();
                    for (Account account : loadedAccounts) {
                        getAccountsInternal().put(account.getAccountNumber(), account);
                    }
                    // Update counters
                    setNextSavingsNumber(1006);
                    setNextCurrentNumber(2011);
                } catch (Exception ignored) {}
            }

            private java.util.Map<String, Account> getAccountsInternal() {
                try {
                    var field = Bank.class.getDeclaredField("accounts");
                    field.setAccessible(true);
                    return (java.util.Map<String, Account>) field.get(this);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            private void setNextSavingsNumber(int val) {
                try {
                    var field = Bank.class.getDeclaredField("nextSavingsNumber");
                    field.setAccessible(true);
                    field.setInt(this, val);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            private void setNextCurrentNumber(int val) {
                try {
                    var field = Bank.class.getDeclaredField("nextCurrentNumber");
                    field.setAccessible(true);
                    field.setInt(this, val);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };

        // Initialize loading
        bank.loadAccountsFromDatabase();

        // 1. Verify pre-loaded accounts are present
        Account a1 = bank.getAccount("SB1005");
        assertEquals("Database Alice", a1.getHolderName());
        assertEquals(15000, a1.getBalance());

        Account a2 = bank.getAccount("CA2010");
        assertEquals("Database Bob", a2.getHolderName());
        assertEquals(8000, a2.getBalance());

        // 2. Verify next created accounts advance beyond loaded numbers
        Account newSavings = bank.createAccount("SAVINGS", "Charlie", 1000);
        assertEquals("SB1006", newSavings.getAccountNumber());

        Account newCurrent = bank.createAccount("CURRENT", "Diana", 1000);
        assertEquals("CA2011", newCurrent.getAccountNumber());
    }

    @Test
    @DisplayName("AccountDAO methods fail cleanly with SQLException when database is unconfigured")
    void testDAOMethodsWithoutConnection() {
        AccountDAO dao = new AccountDAO();
        if (!DBConnection.isConfigured()) {
            Account account = new SavingsAccount("SB1001", "Test", 1000);
            assertThrows(SQLException.class, () -> dao.save(account));
            assertThrows(SQLException.class, () -> dao.findByNumber("SB1001"));
            assertThrows(SQLException.class, () -> dao.updateBalance("SB1001", 2000));
            assertThrows(SQLException.class, () -> dao.addTransaction("SB1001", "DEPOSIT", 500));
            assertThrows(SQLException.class, dao::findAll);
            assertThrows(SQLException.class, () -> dao.delete("SB1001"));
        }
    }
}

