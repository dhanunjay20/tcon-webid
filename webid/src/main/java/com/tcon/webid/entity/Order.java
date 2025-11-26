package com.tcon.webid.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

@Data
@NoArgsConstructor
@Document(collection = "orders")
public class Order {
    @Id
    private String id;
    private String customerId;
    private String vendorOrganizationId; // Set to accepted vendor after bid acceptance
    private String eventName;
    private String eventDate;
    private String eventLocation;
    private int guestCount;
    private List<OrderMenuItem> menuItems;
    private String status;   // pending, confirmed, in_progress, completed, cancelled
    private double totalPrice;
    private String createdAt;
    private String updatedAt;

    @Data
    @NoArgsConstructor
    public static class OrderMenuItem {
        private String menuItemId;
        private String name;
        private int quantity;
        private String specialRequest;
        private double price; // Final price at the time of order
    }
}
