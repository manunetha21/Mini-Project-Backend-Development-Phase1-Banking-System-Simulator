package com.controller;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.dto.AmountRequest;
import com.dto.CreateAccountRequest;
import com.dto.TransferRequest;
import com.model.Account;
import com.model.Transaction;
import com.service.AccountService;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {
    private final AccountService accountService;


    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public ResponseEntity<Account> create(@Valid @RequestBody CreateAccountRequest req) {
        Account a = accountService.createAccount(req.getHolderName());
        return ResponseEntity.created(URI.create("/api/accounts/" + a.getAccountNumber())).body(a);
    }


    @GetMapping("/{accountNumber}")
    public ResponseEntity<Account> get(@PathVariable String accountNumber) {
        return ResponseEntity.ok(accountService.getByAccountNumber(accountNumber));
    }


    @PutMapping("/{accountNumber}/deposit")
    public ResponseEntity<Account> deposit(@PathVariable String accountNumber, @Valid @RequestBody AmountRequest req) {
        return ResponseEntity.ok(accountService.deposit(accountNumber, req.getAmount()));
    }


    @PutMapping("/{accountNumber}/withdraw")
    public ResponseEntity<Account> withdraw(@PathVariable String accountNumber, @Valid @RequestBody AmountRequest req) {
        return ResponseEntity.ok(accountService.withdraw(accountNumber, req.getAmount()));
    }


    @PostMapping("/transfer")
    public ResponseEntity<Map<String, String>> transfer(@RequestBody TransferRequest req) {
        accountService.transfer(req);
        Map<String, String> response = new HashMap<>();
        response.put("status", "SUCCESS");
        response.put("message", "Transfer completed successfully");

        return ResponseEntity.ok(response);
    }


    @GetMapping("/{accountNumber}/transactions")
    public ResponseEntity<List<Transaction>> txns(@PathVariable String accountNumber) {
        return ResponseEntity.ok(accountService.getTransactions(accountNumber));
    }


    @GetMapping
    public ResponseEntity<List<Account>> listAll() {
        return ResponseEntity.ok(accountService.listAllAccounts());
    }
}
