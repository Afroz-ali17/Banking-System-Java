package com.afroz.bank;

public class Main {
    public static void main(String[] args) {
        Account a = new SavingsAccount("SB1001", "Afroz", 10000);
        Account b = new CurrentAccount("CA2001", "Ali", 2000);

        try {
            a.deposit(2500);
            a.withdraw(3000);
            b.withdraw(6000);   // allowed: uses overdraft
            a.withdraw(20000);  // fails: minimum balance rule
        } catch (InsufficientBalanceException e) {
            System.out.println("Transaction failed: " + e.getMessage());
        }

        System.out.println(a);
        System.out.println(b);
        a.getHistory().forEach(System.out::println);
    }
}
