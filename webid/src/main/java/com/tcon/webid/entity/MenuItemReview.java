package com.tcon.webid.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@Document(collection = "item_reviews")
public class MenuItemReview {
    @Id
    private String id;
    private String menuItemId;
    private String userId;
    private String customerName;
    private String reviewDate;
    private String description;
    private int stars;
}
