package com.controller;
import com.main.BankingSystemSpringBootApplication;
import com.dto.AmountRequest;
import com.dto.CreateAccountRequest;
import com.dto.TransferRequest;
import com.model.Account;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;
import com.service.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.mockito.Mock;
@AutoConfigureMockMvc
@SpringBootTest(classes = BankingSystemSpringBootApplication.class)
public class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    //@Mock
    //private AccountService accountService;

    private Account sample = new Account("MC1234", "Manohar");


    @BeforeEach
    void setup() {
        sample = new Account("MC123", "Manohar");
        sample.setBalance(100.0);
    }
    @org.springframework.test.context.bean.override.mockito.MockitoBean
    private AccountService accountService;

    @Test
    void createAccount_validation_and_success() throws Exception {
        CreateAccountRequest req = new CreateAccountRequest();
        req.setHolderName("Manohar");

        Account mockAccount = new Account("MC1234", "Manohar");

        when(accountService.createAccount(anyString()))
                .thenReturn(mockAccount);

        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"holderName":"Manohar"}"""))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/accounts/MC1234"))
                .andExpect(jsonPath("$.holderName").value("Manohar"));
    }


    @Test
    void deposit_validation_badRequest() throws Exception {
        String badJson = """
            {"amount": -10}
            """;

        mockMvc.perform(put("/api/accounts/MC123/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void withdraw_validation_badRequest() throws Exception {
        String badJson = """
            {"amount": 0}
            """;

        mockMvc.perform(put("/api/accounts/MC123/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void transfer_endpoint_success() throws Exception {
        String json = """
            {"fromAccount":"MC1","toAccount":"MC2","amount":100}
            """;

        // service.transfer returns void, so just do nothing
        Mockito.doNothing().when(accountService).transfer(any(TransferRequest.class));

        mockMvc.perform(post("/api/accounts/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    void getAccount_ok() throws Exception {

        Account acc = new Account("MC1234", "Manohar");
        acc.setBalance(1000.0);

        when(accountService.getByAccountNumber(anyString()))
                .thenReturn(acc);

        mockMvc.perform(
                        get("/api/accounts/MC1234")
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value("MC1234"))
                .andExpect(jsonPath("$.holderName").value("Manohar"))
                .andExpect(jsonPath("$.balance").value(1000.0));

        verify(accountService, times(1)).getByAccountNumber("MC1234");
    }
}