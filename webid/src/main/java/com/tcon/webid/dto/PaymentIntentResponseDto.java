package com.tcon.webid.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentIntentResponseDto {

    private String paymentIntentId;

    private String clientSecret;

    private String status;

    private Double amount;

    private String currency;

    private String orderId;

    private String customerId;

    private String message;

    // Publishable key to be consumed by frontend
    private String publishableKey;
}
