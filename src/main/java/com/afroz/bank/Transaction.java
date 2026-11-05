package com.afroz.bank;

import java.time.LocalDateTime;

public class Transaction {
    private final String type;      // DEPOSIT, WITHDRAW, TRANSFER_IN, TRANSFER_OUT
    private final double amount;
    private final LocalDateTime time;

    public Transaction(String type, double amount) {
        this(type, amount, LocalDateTime.now());
    }

    public Transaction(String type, double amount, LocalDateTime time) {
        this.type = type;
        this.amount = amount;
        this.time = (time != null) ? time : LocalDateTime.now();
    }

    public String getType() { return type; }
    public double getAmount() { return amount; }
    public LocalDateTime getTime() { return time; }

    @Override
    public String toString() {
        return time.withNano(0) + " | " + type + " | " + amount;
    }
}
