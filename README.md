# Core Java Banking System

A production-grade, console-based Banking Application built in Core Java designed for campus placement preparation and mastering backend development fundamentals. The system covers Object-Oriented Programming (OOP), Java Collections, custom Exception Handling, Thread Safety & Concurrency, JDBC with MySQL persistence, ACID database transactions, and automated testing with JUnit 5.

---

## Tech Stack

- **Language**: Core Java (JDK 21 LTS)
- **Programming Paradigm**: Object-Oriented Programming (OOP — Abstraction, Encapsulation, Inheritance, Polymorphism)
- **Data Structures**: Java Collections Framework (`HashMap`, `ArrayList`, `Map`, `List`)
- **Concurrency**: Java Concurrency (`synchronized` monitors, `ExecutorService`, `CountDownLatch`, lock hierarchy deadlock prevention)
- **Persistence**: JDBC (Java Database Connectivity) with `PreparedStatement` only
- **Database**: MySQL 8.0+
- **Testing**: JUnit 5 (Jupiter Engine & Surefire)
- **Build Tool**: Apache Maven 3.8+

---

## Features

### 1. Account Hierarchy & Polymorphic Business Rules (OOP)
- **`Account` (Abstract Base Class)**: Encapsulates core fields (`accountNumber`, `holderName`, `balance`, `history`) and thread-safe operations.
- **`SavingsAccount`**: Enforces a strict minimum balance rule (`MIN_BALANCE = 500.0`). Rejects opening balances below 500 and prevents withdrawals that breach the threshold via `InsufficientBalanceException`.
- **`CurrentAccount`**: Provides an overdraft facility up to `5000.0` beyond balance. Allows negative balances representing credit utilized and rejects withdrawals exceeding overdraft capacity.

### 2. High-Performance Account Management (Collections)
- **$O(1)$ Hash Map Lookups**: Uses `HashMap<String, Account>` for constant-time account lookups, insertions, and closures by account number.
- **Automated Sequential Numbering**: Generates `SB1001`, `SB1002`, ... for Savings and `CA2001`, `CA2002`, ... for Current accounts.
- **Conditional Account Closure**: Accounts can only be closed if the balance is exactly `0.0`. Non-zero balances cannot be closed.
- **Listing Accounts**: View all registered bank accounts with current balances.

### 3. Inter-Account Transfers & Rollback Handling
- **Transfer Validation**: Rejects transfers with non-positive amounts, identical source/target accounts, or unknown accounts (`AccountNotFoundException`).
- **Audit Trail Customization**: Records `TRANSFER_OUT` on the source account and `TRANSFER_IN` on the target account (not `WITHDRAW` and `DEPOSIT`).
- **Manual Rollback**: If target deposit fails after withdrawal, source balance and transaction history are automatically restored.

### 4. Interactive Console User Interface (`BankApp`)
- Menu-driven command-line interface powered by `java.util.Scanner`:
  - `1`: Create Account
  - `2`: Deposit
  - `3`: Withdraw
  - `4`: Transfer
  - `5`: Check Balance
  - `6`: Mini Statement (recent transaction history with timestamps and types)
  - `7`: Close Account
  - `0`: Exit
- **Crash-Proof Design**: Robust `try/catch` wrapping for `InsufficientBalanceException`, `AccountNotFoundException`, `IllegalArgumentException`, and `InputMismatchException` (drains invalid scanner tokens so bad input never crashes the app).

### 5. Multithreading & Deadlock Prevention
- **Mutual Exclusion**: `synchronized` methods on `Account` prevent lost updates during concurrent operations (verified with 10 threads running 1,000 simultaneous deposits).
- **Global Lock Ordering in `transfer()`**: Eliminates circular-wait deadlocks during simultaneous bi-directional transfers ($A \rightarrow B$ and $B \rightarrow A$) by deterministically acquiring account locks sorted lexicographically by account number.

### 6. MySQL Database Persistence via JDBC
- **Zero Hardcoded Credentials**: [`DBConnection`](src/main/java/com/afroz/bank/DBConnection.java) reads `DB_URL`, `DB_USER`, and `DB_PASSWORD` dynamically from environment variables.
- **SQL Injection Prevention**: [`AccountDAO`](src/main/java/com/afroz/bank/AccountDAO.java) uses `PreparedStatement` exclusively.
- **ACID Database Transactions**: `transfer()` manages explicit transactions using `setAutoCommit(false)`, updates both account balances and logs transfer transactions, and triggers `commit()` on success or `rollback()` on any failure.
- **Startup Database Loading**: `Bank` loads accounts and historical transactions from MySQL on startup and advances ID counters to prevent primary key collisions.

---

## Project Structure

```text
bank/
├── pom.xml
├── .gitignore
├── README.md
├── schema.sql
└── src/
    ├── main/
    │   └── java/
    │       └── com/afroz/bank/
    │           ├── Account.java
    │           ├── AccountDAO.java
    │           ├── AccountNotFoundException.java
    │           ├── Bank.java
    │           ├── BankApp.java
    │           ├── ConcurrencyDemo.java
    │           ├── CurrentAccount.java
    │           ├── DBConnection.java
    │           ├── InsufficientBalanceException.java
    │           ├── Main.java
    │           ├── SavingsAccount.java
    │           └── Transaction.java
    └── test/
        └── java/
            └── com/afroz/bank/
                ├── AccountDAOTest.java
                ├── AccountTest.java
                ├── BankAppTest.java
                ├── BankTest.java
                ├── ConcurrencyTest.java
                └── DBConnectionTest.java
```

---

## Database Schema (`schema.sql`)

```sql
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

CREATE INDEX idx_transactions_account ON transactions(account_number);
```

---

## Setup and Run Steps

### Prerequisites
- **Java 21 (JDK 21 LTS)**
- **Apache Maven 3.8+**
- **MySQL Server 8.0+** & **MySQL Workbench**

---

### Step 1: Database Setup in MySQL Workbench
1. Open **MySQL Workbench** and connect to your local MySQL server (`localhost:3306`).
2. Open `schema.sql` (`Ctrl + Shift + O` or `File` -> `Open SQL Script...`).
3. Click the **Execute** button (`⚡`) to create the `bank_db` database and tables.

---

### Step 2: Set Environment Variables on Windows

#### Option A: Windows PowerShell (Current Session)
```powershell
$env:DB_URL = "jdbc:mysql://localhost:3306/bank_db"
$env:DB_USER = "root"
$env:DB_PASSWORD = "your_mysql_password"
```

#### Option B: Windows System Properties (Permanent)
1. Press `Win + R`, type `sysdm.cpl`, and press **Enter**.
2. Go to **Advanced** tab -> **Environment Variables...**.
3. Under **User variables**, click **New...** and add:
   - `DB_URL` = `jdbc:mysql://localhost:3306/bank_db`
   - `DB_USER` = `root`
   - `DB_PASSWORD` = `your_mysql_password`
4. Click **OK** and restart your terminal.

*(Note: If environment variables are not set, the application operates in in-memory mode seamlessly).*

---

### Step 3: Run the Application

#### Run Interactive Console App (`BankApp`) with Maven:
```bash
mvn compile exec:java
```

#### Run with Direct Java Compilation (without Maven):
```powershell
# Compile classes into 'out'
javac -d out (Get-ChildItem -Recurse src/main/java/*.java | % FullName)

# Run interactive console application
java -cp out com.afroz.bank.BankApp

# Run automated Days 1-3 demo
java -cp out com.afroz.bank.Main

# Run Day 5 multithreading concurrency demo
java -cp out com.afroz.bank.ConcurrencyDemo
```

#### Package Executable JAR and Run:
```bash
mvn clean package
java -jar target/bank-system-1.0-SNAPSHOT.jar
```

---

### Step 4: Run Automated Tests
```bash
mvn clean test
```
Runs all 31 JUnit 5 unit tests across `AccountTest`, `BankTest`, `BankAppTest`, `ConcurrencyTest`, `AccountDAOTest`, and `DBConnectionTest`.

---

## Sample Console Output

### 1. Interactive Console UI (`BankApp`)
```text
==================================================
          WELCOME TO AFROZ BANKING SYSTEM         
==================================================

--------------------------------------------------
                   MAIN MENU                      
--------------------------------------------------
  1. Create Account
  2. Deposit
  3. Withdraw
  4. Transfer
  5. Check Balance
  6. Mini Statement
  7. Close Account
  0. Exit
--------------------------------------------------
Enter your choice (0-7): 1

--- [1] Create New Account ---
Enter Account Type (1 for SAVINGS, 2 for CURRENT): 1
Enter Account Holder Name: Afroz Ali
Enter Opening Balance: 10000

[Success] Account created successfully!
  Account Number : SB1001
  Holder Name    : Afroz Ali
  Current Balance: 10000.00

--------------------------------------------------
Enter your choice (0-7): 2

--- [2] Deposit Funds ---
Enter Account Number: SB1001
Enter Deposit Amount: 2500

[Success] Deposited 2500.00 into SB1001. New Balance: 12500.00

--------------------------------------------------
Enter your choice (0-7): 6

--- [6] Mini Statement ---
Enter Account Number: SB1001

[Mini Statement for SB1001 (Afroz Ali)]
Current Balance: 12500.00
Transaction History:
   1. 2026-11-06T18:55:00 | DEPOSIT | 10000.0
   2. 2026-11-06T18:55:10 | DEPOSIT | 2500.0
```

### 2. Concurrency & Thread-Safety Demo (`ConcurrencyDemo`)
```text
==================================================
        DAY 5: MULTITHREADING & CONCURRENCY       
==================================================

--- 1. Concurrent Deposits (Thread Safety Proof) ---
Initial Account State: CA2001 | Concurrency Tester | Balance: 0.0
Submitting 1000 deposit tasks of 1.00 across 10 threads in ExecutorService...
All deposit tasks completed in 46 ms.
Expected Balance : 1000.00
Actual Balance   : 1000.00
Recorded History : 1000 transactions
[RESULT] SUCCESS: Zero lost updates! Synchronized deposit is thread-safe.

--- 2. Deadlock-Free Concurrent Transfers ---
Account A: CA2001 | Alice | Balance: 10000.0
Account B: CA2002 | Bob | Balance: 10000.0
Total System Balance Before: 20000.00
Running concurrent bi-directional transfers (1000 each A->B and B->A)...
[RESULT] SUCCESS: All 2000 transfers completed without deadlock!
Account A Final: CA2001 | Alice | Balance: 10000.0
Account B Final: CA2002 | Bob | Balance: 10000.0
Total System Balance After : 20000.00
Conservation of Money Check: PASSED (20000.00 == 20000.00)
```

---

## Key Interview Discussion Topics

1. **`HashMap` vs `List`**:
   `HashMap` offers $O(1)$ constant-time retrieval by account number using key hashing. A `List` requires linear $O(N)$ searches, which degrades performance as accounts scale into thousands or millions.
2. **Race Conditions & Mutual Exclusion**:
   Non-synchronized read-modify-write operations (`balance += amount`) result in lost updates due to thread interleaving and CPU cache visibility lag. `synchronized` provides both mutual exclusion and a happens-before memory barrier.
3. **Deadlock Prevention (Global Lock Ordering)**:
   Concurrent transfers between Account A and Account B ($A \rightarrow B$ vs $B \rightarrow A$) can cause circular wait. Enforcing a deterministic lock hierarchy by comparing account numbers eliminates circular wait, ensuring deadlock freedom.
4. **ACID Transactions in Banking**:
   Inter-account transfers must either complete both balance adjustments and transaction records or complete none. Disabling auto-commit (`setAutoCommit(false)`) guarantees atomicity via explicit `commit()` and `rollback()`.
