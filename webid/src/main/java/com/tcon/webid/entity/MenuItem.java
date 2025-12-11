package com.tcon.webid.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

@Data
@NoArgsConstructor
@Document(collection = "menu_items")
public class MenuItem {
    @Id
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
