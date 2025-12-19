package com.tcon.webid.dto.admin;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminBidDto {
    private String id;
    private String orderId;
    private String vendorOrganizationId;
    private String proposedMessage;
    private double proposedTotalPrice;
    private String status;
    private String submittedAt;
    private String updatedAt;
    private String customerName;
    private String vendorBusinessName;
    private String eventName;
}

