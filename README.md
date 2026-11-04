# Banking System (Core Java)

Console banking application built to practice OOP, Collections, Exception Handling,
Multithreading, and JDBC for campus placement interview preparation.

## Prerequisites
- Java 21 (JDK 21)
- Apache Maven 3.8+

## Project Structure
```text
bank/
├── pom.xml
├── .gitignore
├── README.md
└── src/
    ├── main/
    │   └── java/
    │       └── com/afroz/bank/
    │           ├── Account.java
    │           ├── AccountNotFoundException.java
    │           ├── Bank.java
    │           ├── BankApp.java
    │           ├── ConcurrencyDemo.java
    │           ├── CurrentAccount.java
    │           ├── InsufficientBalanceException.java
    │           ├── Main.java
    │           ├── SavingsAccount.java
    │           └── Transaction.java
    └── test/
        └── java/
            └── com/afroz/bank/
                ├── AccountTest.java
                ├── BankAppTest.java
                ├── BankTest.java
                └── ConcurrencyTest.java
```

## How to Build and Run

### 1. Run tests with Maven
```bash
mvn clean test
```

### 2. Run the application with Maven
```bash
mvn compile exec:java
```

### 3. Build JAR and Run
```bash
mvn clean package
java -jar target/bank-system-1.0-SNAPSHOT.jar
```

### 4. Direct Java Compilation (without Maven)
```bash
# Windows PowerShell
javac -d out (Get-ChildItem -Recurse src/main/java/*.java | % FullName)
java -cp out com.afroz.bank.BankApp

# Or to run the automated Days 1-3 demo suite:
java -cp out com.afroz.bank.Main

# Or to run the Day 5 Concurrency & Thread-Safety demo:
java -cp out com.afroz.bank.ConcurrencyDemo
```

## Day 5: Concurrency & Thread Safety Observations
Without synchronization, concurrent execution of non-atomic read-modify-write operations (`balance += amount`) causes lost updates due to thread interleaving and CPU cache visibility lag, yielding inconsistent balances (typically between 970.00 and 999.00 instead of 1000.00). Restoring `synchronized` on `deposit` guarantees mutual exclusion and memory visibility, ensuring an exact final balance of 1000.00. Furthermore, circular-wait deadlocks in inter-account transfers are permanently prevented by enforcing a global lock hierarchy—sorting accounts deterministically by account number before acquiring monitors—allowing simultaneous bi-directional transfers ($A \rightarrow B$ and $B \rightarrow A$) to execute safely without stalling.
