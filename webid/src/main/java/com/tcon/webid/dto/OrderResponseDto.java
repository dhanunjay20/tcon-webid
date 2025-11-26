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
    private String vendorOrganizationId;
    private String vendorBusinessName;
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
