package com.tcon.webid.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@Document(collection = "notifications")
public class Notification {
    @Id
    private String id;
    private String recipientUserId;         // Who receives this notification (user or vendor user)
    private String recipientVendorOrgId;    // Optionally for targeting organization/vendor
    private String type;                    // e.g. "BID_RECEIVED", "BID_ACCEPTED", "BID_REJECTED", "ORDER_STATUS"
    private String message;                 // Human-friendly for UI
    private String dataId;                  // Related entity (bidId, orderId, etc.)
    private String dataType;                // "order", "bid", "menu", etc.
    private boolean read;                   // Show as unread until opened
    private String createdAt;
}
