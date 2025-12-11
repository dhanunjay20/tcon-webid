package com.tcon.webid.dto;

import lombok.Data;
import java.util.List;

@Data
public class MenuItemResponseDto {
    private String id;
    private String vendorOrganizationId;
    private String name;
    private String description;
    private List<String> images;
    private String category;
    private String subCategory;
    private List<String> ingredients;
    private List<String> spiceLevels;
    private boolean available;
}
