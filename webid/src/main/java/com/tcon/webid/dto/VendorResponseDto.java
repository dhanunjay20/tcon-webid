package com.tcon.webid.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.tcon.webid.entity.Address;
import com.tcon.webid.entity.LicenseDocument;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VendorResponseDto {
    private String id;
    private String vendorOrganizationId;
    private String businessName;
    private String contactName;
    private String email;
    private String mobile;
    private List<Address> addresses;
    private List<LicenseDocument> licenseDocuments;
    private Boolean isOnline; // Vendor online status
    private String lastSeenAt; // Last seen timestamp
    
    // Additional business information
    private String website;
    private Integer yearsInBusiness;
    private String aboutBusiness;

    // Live location fields
    private Double latitude;
    private Double longitude;
    private String lastLocationUpdatedAt;

    // Distance field (calculated for radius search, in km or miles)
    private Double distance;
}
