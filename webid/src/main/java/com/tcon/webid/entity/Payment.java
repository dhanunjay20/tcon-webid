package com.tcon.webid.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "payments")
public class Payment {
    @Id
    private String id;

    @Indexed
    private String orderId;

    @Indexed
    private String customerId;

    private String vendorOrganizationId;

    @Indexed(unique = true)
    private String stripePaymentIntentId;

    private String stripeCustomerId;

    private String stripeChargeId;

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

    private String refundId;

    private Double refundAmount;

    private String refundReason;

    private Instant createdAt;

    private Instant updatedAt;

    private Instant paidAt;

    private String ipAddress;

    private String userAgent;
}

