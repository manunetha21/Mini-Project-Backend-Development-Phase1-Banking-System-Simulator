package com.service;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.exception.AccountNotFoundException;
import com.exception.InsufficientBalanceException;
import com.exception.InvalidAmountException;
import com.model.Account;
import com.model.Transaction;
import com.dto.TransferRequest;
import com.repository.AccountRepository;
import com.repository.TransactionRepository;
import com.service.AccountService;

@Service
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    private final ConcurrentMap<String, Object> locks = new ConcurrentHashMap<>();

    public AccountServiceImpl(AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    private Object lockFor(String acc) {
        return locks.computeIfAbsent(acc, k -> new Object());
    }
    private String generateAccountNumber(String name) {
        String[] parts = name.trim().split("\\s+");
        StringBuilder initials = new StringBuilder();
        for (String p : parts) if (!p.isEmpty()) initials.append(Character.toUpperCase(p.charAt(0)));
        int num = (int)(Math.random() * 9000) + 1000;
        return initials.toString() + num;
    }
    @Override
    public Account createAccount(String holderName) {
        if (holderName == null || holderName.trim().isEmpty()) throw new InvalidAmountException("Holder name required");
        String accNum = generateAccountNumber(holderName);
        while (accountRepository.existsByAccountNumber(accNum)) accNum = generateAccountNumber(holderName);
        Account a = new Account(accNum, holderName.trim());
        accountRepository.save(a);
        return a;
    }
    @Override
    public Account getByAccountNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + accountNumber));
    }

    @Override
    @Transactional
    public Account deposit(String accountNumber, Double amount) {
        if (amount == null || amount <= 0)
            throw new InvalidAmountException("Amount must be positive");

        Account acc = getByAccountNumber(accountNumber);

        synchronized (lockFor(acc.getAccountNumber())) {

            // update balance
            acc.setBalance(acc.getBalance() + amount);

            // create transaction
            Transaction t = new Transaction(
                    UUID.randomUUID().toString(),
                    "DEPOSIT",
                    amount,
                    "SUCCESS",
                    null,
                    accountNumber
            );

            transactionRepository.save(t);

            // add transaction id BEFORE saving account
            acc.getTransactionIds().add(t.getTransactionId());

            // NOW only ONE save
            return accountRepository.save(acc);
        }
    }


    @Override
    @Transactional
    public Account withdraw(String accountNumber, Double amount) {
        if (amount == null || amount <= 0) throw new InvalidAmountException("Amount must be positive");
        Account acc = getByAccountNumber(accountNumber);
        synchronized (lockFor(acc.getAccountNumber())) {
            if (acc.getBalance() < amount) throw new InsufficientBalanceException("Insufficient balance");
            acc.setBalance(acc.getBalance() - amount);
            accountRepository.save(acc);
            Transaction t = new Transaction(UUID.randomUUID().toString(), "WITHDRAW", amount, "SUCCESS", accountNumber, null);
            transactionRepository.save(t);
            acc.getTransactionIds().add(t.getTransactionId());
            accountRepository.save(acc);
            return acc;
        }
    }
    @Transactional
    @Override
    public void transfer(TransferRequest request) {

        String fromAcc = request.getFromAccount();
        String toAcc   = request.getToAccount();
        Double amount  = request.getAmount();

        if (fromAcc.equals(toAcc)) {
            throw new InvalidAmountException("Source and destination same");
        }

        if (amount == null || amount <= 0) {
            throw new InvalidAmountException("Amount must be positive");
        }


        //avoid deadlock
        String first = fromAcc.compareTo(toAcc) < 0 ? fromAcc : toAcc;
        String second = first.equals(fromAcc) ? toAcc : fromAcc;


        synchronized (lockFor(first)) {
            synchronized (lockFor(second)) {
                Account src = getByAccountNumber(fromAcc);
                Account dst = getByAccountNumber(toAcc);
                if (src.getBalance() < amount) throw new InsufficientBalanceException("Insufficient balance in source account");
                src.setBalance(src.getBalance() - amount);
                dst.setBalance(dst.getBalance() + amount);
                accountRepository.save(src);
                accountRepository.save(dst);
                Transaction t = new Transaction(UUID.randomUUID().toString(), "TRANSFER", amount, "SUCCESS", fromAcc, toAcc);
                transactionRepository.save(t);
                src.getTransactionIds().add(t.getTransactionId());
                dst.getTransactionIds().add(t.getTransactionId());
                accountRepository.save(src);
                accountRepository.save(dst);
            }
        }
    }


    @Override
    public List<Transaction> getTransactions(String accountNumber) {
        return transactionRepository.findBySourceAccountOrDestinationAccount(accountNumber, accountNumber);
    }


    @Override
    public List<Account> listAllAccounts() {
        return accountRepository.findAll();
    }
}