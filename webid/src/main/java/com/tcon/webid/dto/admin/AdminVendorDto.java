package com.tcon.webid.dto.admin;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import com.tcon.webid.entity.Address;
import com.tcon.webid.entity.LicenseDocument;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminVendorDto {
    private String id;
    private String vendorOrganizationId;
    private String businessName;
    private String contactName;
    private String email;
    private String mobile;
    private List<Address> addresses;
    private List<LicenseDocument> licenseDocuments;
    private Boolean isOnline;
    private String lastSeenAt;
    private String website;
    private Integer yearsInBusiness;
    private String aboutBusiness;
    private Double latitude;
    private Double longitude;
    private String lastLocationUpdatedAt;
}

