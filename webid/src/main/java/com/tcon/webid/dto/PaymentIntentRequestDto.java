package com.tcon.webid.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentIntentRequestDto {

    @NotBlank(message = "Order ID is required")
    private String orderId;
    // PaymentIntentRequestDto.java
    private String customerId; // optional: MongoDB user id
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private Double amount; // in dollars

    private String currency = "usd";

    private String description;

    // Optional: Save payment method for future use
    private Boolean savePaymentMethod = false;

    // Customer info for Stripe customer creation
    private String customerEmail;

    private String customerName;
}

