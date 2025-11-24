package com.dto;
import jakarta.validation.constraints.Positive;

public class AmountRequest {
    @Positive(message = "Amount must be positive")
    private Double amount;
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) {
        this.amount = amount;
    }
}