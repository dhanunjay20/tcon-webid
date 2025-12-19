package com.tcon.webid.dto.admin;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminMenuItemDto {
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
    private String vendorName;
}

