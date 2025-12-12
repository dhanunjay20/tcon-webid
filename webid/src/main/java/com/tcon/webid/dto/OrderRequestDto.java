package com.tcon.webid.dto;

import lombok.Data;
import java.util.List;

@Data
public class OrderRequestDto {
    private String customerId;
    private String eventName;
    private String eventDate;
    private String eventLocation;
    private int guestCount;
    private List<OrderMenuItemDto> menuItems;
    private double totalPrice;

    @Data
    public static class OrderMenuItemDto {
        private String menuItemId;
        private String specialRequest;
    }
}
