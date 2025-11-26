package com.tcon.webid.dto;

import lombok.Data;

@Data
public class BidRequestDto {
    private String orderId;
    private String vendorOrganizationId;
    private String proposedMessage;
    private double proposedTotalPrice;

    // Optional snapshot fields (convenience)
    private String customerName;
    private String vendorBusinessName;
    private String eventName;
}
