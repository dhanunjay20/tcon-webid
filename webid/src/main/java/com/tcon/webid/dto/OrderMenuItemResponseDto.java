package com.tcon.webid.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderMenuItemResponseDto {
    private String menuItemId;
    private String name; // name from order (or menu item)
    private String specialRequest;

    // menu item details (flat structure)
    private String vendorOrganizationId;
    private String description;
    private List<String> images;
    private String category;
    private String subCategory;
    private List<String> ingredients;
    private List<String> spiceLevels;
    private Boolean available;
}
