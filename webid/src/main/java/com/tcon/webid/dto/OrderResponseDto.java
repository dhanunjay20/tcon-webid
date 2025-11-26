package com.tcon.webid.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponseDto {
    private String id;
    private String customerId;

    // Customer contact info (populated for vendor responses)
    private String userName;
    private String userEmail;
    private String userPhone;

    private String vendorOrganizationId;
    private String vendorBusinessName;
    // Vendor contact info (populated for user responses)
    private String vendorEmail;
    private String vendorPhone;

    private String eventName;
    private String eventDate;
    private String eventLocation;
    private int guestCount;
    private List<OrderMenuItemResponseDto> menuItems;
    private String status;
    private double totalPrice;
    private String createdAt;
    private String updatedAt;
}
