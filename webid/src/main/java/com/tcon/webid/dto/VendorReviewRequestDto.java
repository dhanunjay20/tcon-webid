package com.tcon.webid.dto;

import lombok.Data;

@Data
public class VendorReviewRequestDto {
    private String vendorOrganizationId;
    private String customerName;
    private String reviewDate;
    private String description;
    private int stars;
}
