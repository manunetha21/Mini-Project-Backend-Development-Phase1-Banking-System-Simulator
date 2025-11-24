package com.service;
import com.main.BankingSystemSpringBootApplication;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;  // unit
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.junit.jupiter.api.Assertions;
import com.repository.AccountRepository;
import com.repository.TransactionRepository;
import com.model.Account;
import java.util.Optional;
import com.model.Transaction;
import org.mockito.*;
import com.exception.AccountNotFoundException;
import com.exception.InsufficientBalanceException;
import com.exception.InvalidAmountException;
import java.util.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import com.exception.InvalidAmountException;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

@AutoConfigureMockMvc
@SpringBootTest(classes = BankingSystemSpringBootApplication.class)
public class AccountServiceImpTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private AccountServiceImpl svc;
    @Test
    void createAccount_success() {
        String holder = "Manohar Cheruku";
        when(accountRepository.existsByAccountNumber(anyString())).thenReturn(false);
        // save returns the saved account, simulate repository behavior:
        ArgumentCaptor<Account> cap = ArgumentCaptor.forClass(Account.class);
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        Account a = svc.createAccount(holder);

        assertNotNull(a.getAccountNumber());
        assertEquals("Manohar Cheruku", a.getHolderName());
        verify(accountRepository, times(1)).save(cap.capture());
        assertEquals(holder, cap.getValue().getHolderName());
    }

    @Test
    void createAccount_invalidName() {
        assertThrows(InvalidAmountException.class, () -> svc.createAccount("   "));
    }
    @Test
    void getByAccountNumber_found() {
        Account a = new Account("MC1", "X");
        when(accountRepository.findByAccountNumber("MC1")).thenReturn(Optional.of(a));
        Account r = svc.getByAccountNumber("MC1");
        assertSame(a, r);
    }

    @Test
    void getByAccountNumber_notFound() {
        when(accountRepository.findByAccountNumber("NO")).thenReturn(Optional.empty());
        assertThrows(AccountNotFoundException.class, () -> svc.getByAccountNumber("NO"));
    }

    @Test
    void deposit_success_and_transaction_created() {
        String accNo = "MC1";
        Account a = new Account(accNo, "X");
        a.setBalance(100.0);
        when(accountRepository.findByAccountNumber(accNo)).thenReturn(Optional.of(a));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArgument(0));
        when(accountRepository.save(any(Account.class))).thenAnswer(i -> i.getArgument(0));

        Account res = svc.deposit(accNo, 50.0);

        assertEquals(150.0, res.getBalance());
        assertFalse(res.getTransactionIds().isEmpty());
        verify(transactionRepository, times(1)).save(any(Transaction.class));
        verify(accountRepository, times(1)).save(res);
    }

    @Test
    void deposit_invalidAmount() {
        assertThrows(InvalidAmountException.class, () -> svc.deposit("MC1", -10.0));
        assertThrows(InvalidAmountException.class, () -> svc.deposit("MC1", null));
    }

    @Test
    void withdraw_success() {
        String accNo = "W1";
        Account a = new Account(accNo, "W");
        a.setBalance(500.0);
        when(accountRepository.findByAccountNumber(accNo)).thenReturn(Optional.of(a));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArgument(0));
        when(accountRepository.save(any(Account.class))).thenAnswer(i -> i.getArgument(0));

        Account res = svc.withdraw(accNo, 200.0);
        assertEquals(300.0, res.getBalance());
        verify(transactionRepository, times(1)).save(any(Transaction.class));
        verify(accountRepository, atLeastOnce()).save(any(Account.class));
    }

    @Test
    void withdraw_insufficientBalance() {
        String accNo = "W2";
        Account a = new Account(accNo, "W");
        a.setBalance(50.0);
        when(accountRepository.findByAccountNumber(accNo)).thenReturn(Optional.of(a));
        assertThrows(InsufficientBalanceException.class, () -> svc.withdraw(accNo, 100.0));
    }

    @Test
    void transfer_success() {
        String from = "A1", to = "A2";
        Account a1 = new Account(from, "A");
        a1.setBalance(1000.0);
        Account a2 = new Account(to, "B");
        a2.setBalance(100.0);

        when(accountRepository.findByAccountNumber(from)).thenReturn(Optional.of(a1));
        when(accountRepository.findByAccountNumber(to)).thenReturn(Optional.of(a2));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArgument(0));
        when(accountRepository.save(any(Account.class))).thenAnswer(i -> i.getArgument(0));
        com.dto.TransferRequest req = new com.dto.TransferRequest();
        req.setFromAccount(from); req.setToAccount(to); req.setAmount(300.0);

        svc.transfer(req);

        assertEquals(700.0, a1.getBalance());
        assertEquals(400.0, a2.getBalance());
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void transfer_sameAccount_throws() {
        com.dto.TransferRequest req = new com.dto.TransferRequest();
        req.setFromAccount("X"); req.setToAccount("X"); req.setAmount(10.0);
        assertThrows(InvalidAmountException.class, () -> svc.transfer(req));
    }

    @Test
    void transfer_insufficient_throws() {
        String from = "S1", to = "S2";
        Account a1 = new Account(from, "A"); a1.setBalance(10.0);
        Account a2 = new Account(to, "B"); a2.setBalance(0.0);
        when(accountRepository.findByAccountNumber(from)).thenReturn(Optional.of(a1));
        when(accountRepository.findByAccountNumber(to)).thenReturn(Optional.of(a2));

        com.dto.TransferRequest req = new com.dto.TransferRequest();
        req.setFromAccount(from); req.setToAccount(to); req.setAmount(100.0);

        assertThrows(InsufficientBalanceException.class, () -> svc.transfer(req));
    }
    @Test
    void listAllAccounts_and_getTransactions() {
        Account a = new Account("L1","Z");
        when(accountRepository.findAll()).thenReturn(List.of(a));
        when(transactionRepository.findBySourceAccountOrDestinationAccount("L1","L1")).thenReturn(List.of());

        assertEquals(1, svc.listAllAccounts().size());
        assertNotNull(svc.getTransactions("L1"));
    }


}
