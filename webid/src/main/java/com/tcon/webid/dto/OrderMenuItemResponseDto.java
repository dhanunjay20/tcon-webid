package com.tcon.webid.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderMenuItemResponseDto {
    private String menuItemId;
    private String specialRequest;

    // Embedded menu item details
    private MenuItemResponseDto menuItem;
}
