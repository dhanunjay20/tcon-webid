package com.tcon.webid.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * DTO for real-time bid update notifications via WebSocket
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BidUpdateNotification {
    private String bidId;
    private String orderId;
    private String vendorOrganizationId;
    private String status; // requested, quoted, accepted, rejected
    private String eventType; // BID_CREATED, BID_UPDATED, BID_DELETED, BID_QUOTED, BID_ACCEPTED, BID_REJECTED
    private String message;
    private double proposedTotalPrice;
    private String customerName;
    private String vendorBusinessName;
    private String eventName;
    private String timestamp;

    // Add default timestamp on creation
    public static BidUpdateNotificationBuilder builder() {
        return new BidUpdateNotificationBuilder().timestamp(Instant.now().toString());
    }
}


