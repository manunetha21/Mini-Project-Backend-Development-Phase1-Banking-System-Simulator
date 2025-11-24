package com.model;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "Account")
public class Account {
    @Id
    private String id;

    @Indexed(unique = true)
    private String accountNumber;

    private String holderName;
    private Double balance = 0.0;
    private String status = "ACTIVE";
    private Instant createdAt = Instant.now();

    private List<String> transactionIds = new ArrayList<>();

    public Account() {}

    public Account(String accountNumber, String holderName) {
        this.accountNumber = accountNumber;
        this.holderName = holderName;
        this.balance = 0.0;
        this.createdAt = Instant.now();
    }
    public Account(String accountNumber, String holderName, Double balance) {
        this.accountNumber = accountNumber;
        this.holderName = holderName;
        this.balance = balance;
        this.createdAt = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
    public String getHolderName() { return holderName; }
    public void setHolderName(String holderName) { this.holderName = holderName; }
    public Double getBalance() { return balance; }
    public void setBalance(Double balance) { this.balance = balance; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public List<String> getTransactionIds() { return transactionIds; }
    public void setTransactionIds(List<String> transactionIds) { this.transactionIds = transactionIds; }
}
