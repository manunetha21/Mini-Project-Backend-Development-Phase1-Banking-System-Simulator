package com.service;

import java.util.List;

import com.dto.TransferRequest;
import com.model.Account;
import com.model.Transaction;

public interface AccountService {
    Account createAccount(String holderName);
    Account getByAccountNumber(String accountNumber);
    Account deposit(String accountNumber, Double amount);
    Account withdraw(String accountNumber, Double amount);
    void transfer(TransferRequest req);
    List<Transaction> getTransactions(String accountNumber);
    List<Account> listAllAccounts();
}