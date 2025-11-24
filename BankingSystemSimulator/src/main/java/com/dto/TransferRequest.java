package com.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public class TransferRequest {
    @NotBlank
    private String fromAccount;
    @NotBlank
    private String toAccount;
    @Positive
    private Double amount;

    public String getFromAccount() {
        return fromAccount;
    }
    public void setFromAccount(String fromAccount) {
        this.fromAccount = fromAccount;
    }
    public String getToAccount() {
        return toAccount;
    }
    public void setToAccount(String toAccount) {
        this.toAccount = toAccount;
    }
    public Double getAmount() {
        return amount;
    }
    public void setAmount(Double amount) {
        this.amount = amount;
    }
}