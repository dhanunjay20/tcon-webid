package com.tcon.webid.dto;

import lombok.Data;

@Data
public class MenuItemReviewRequestDto {
    private String menuItemId;
    private String customerName;
    private String reviewDate;
    private String description;
    private int stars;
}
