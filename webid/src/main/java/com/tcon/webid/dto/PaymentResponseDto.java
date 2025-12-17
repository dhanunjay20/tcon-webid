package com.tcon.webid.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponseDto {

    private String id;

    private String orderId;

    private String customerId;

    private String vendorOrganizationId;

    private String stripePaymentIntentId;

    private Double amount;

    private String currency;

    private String status;

    private String paymentMethod;

    private String paymentMethodType;

    private String last4;

    private String brand;

    private String description;

    private String receiptUrl;

    private String failureReason;

    private Instant createdAt;

    private Instant updatedAt;

    private Instant paidAt;
}

