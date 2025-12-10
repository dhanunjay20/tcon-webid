package com.tcon.webid.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * DTO for real-time order update notifications via WebSocket
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderUpdateNotification {
    private String orderId;
    private String customerId;
    private String vendorId; // MongoDB _id of the vendor
    private String vendorOrganizationId; // Business organization ID (kept for backward compatibility)
    private String eventName;
    private String eventDate;
    private String eventLocation;
    private int guestCount;
    private String status; // pending, confirmed, in_progress, completed, cancelled
    private double totalPrice;
    private String eventType; // ORDER_CREATED, ORDER_UPDATED, ORDER_DELETED, ORDER_STATUS_CHANGED
    private String message;
    private String timestamp;

    // Add default timestamp on creation
    public static OrderUpdateNotificationBuilder builder() {
        return new OrderUpdateNotificationBuilder().timestamp(Instant.now().toString());
    }
}

