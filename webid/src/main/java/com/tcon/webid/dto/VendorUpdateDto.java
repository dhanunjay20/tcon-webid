package com.tcon.webid.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.tcon.webid.entity.Address;
import com.tcon.webid.entity.LicenseDocument;
import java.util.List;

/**
 * DTO for updating vendor profile information
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VendorUpdateDto {

    private String businessName;
    private String contactName;
    private String email;
    private String mobile;
    private String password; // Optional: only if updating password
    private List<Address> addresses;
    private List<LicenseDocument> licenseDocuments;

    // New fields from ServiceDetails (for convenience)
    private String website;
    private Integer yearsInBusiness;
    private String aboutBusiness;
}

