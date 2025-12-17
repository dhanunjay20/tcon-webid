package com.tcon.webid.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefundRequestDto {

    @NotBlank(message = "Payment ID is required")
    private String paymentId;

    @Positive(message = "Amount must be positive")
    private Double amount; // Partial refund amount, null for full refund

    private String reason; // duplicate, fraudulent, requested_by_customer

    private String description;
}

