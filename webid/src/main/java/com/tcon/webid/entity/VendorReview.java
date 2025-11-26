package com.tcon.webid.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@Document(collection = "vendor_reviews")
public class VendorReview {
    @Id
    private String id;
    private String vendorOrganizationId;
    private String userId;
    private String customerName;
    private String reviewDate;
    private String description;
    private int stars;
}
