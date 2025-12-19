package com.tcon.webid.dto.admin;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminPaymentDto {
    private String id;
    private String orderId;
    private String customerId;
    private String vendorOrganizationId;
    private String stripePaymentIntentId;
    private String stripeCustomerId;
    private String stripeChargeId;
    private Long amountInCents;
    private String currency;
    private String status;
    private Instant paidAt;
    private Instant createdAt;
    private String customerName;
    private String vendorName;
}

