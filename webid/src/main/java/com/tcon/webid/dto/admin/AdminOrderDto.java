package com.tcon.webid.dto.admin;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import com.tcon.webid.entity.Order;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminOrderDto {
    private String id;
    private String customerId;
    private String vendorOrganizationId;
    private String eventName;
    private String eventDate;
    private String eventLocation;
    private int guestCount;
    private List<Order.OrderMenuItem> menuItems;
    private String status;
    private double totalPrice;
    private String createdAt;
    private String updatedAt;

    // Additional info for admin view
    private String customerName;
    private String vendorName;
}

