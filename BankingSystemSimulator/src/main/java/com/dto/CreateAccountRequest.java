package com.dto;
import jakarta.validation.constraints.NotBlank;

public class CreateAccountRequest {
    @NotBlank(message = "Holder name is required")
    private String holderName;
    public String getHolderName() {
        return holderName;
    }
    public void setHolderName(String holderName) {
        this.holderName = holderName;
    }
}
