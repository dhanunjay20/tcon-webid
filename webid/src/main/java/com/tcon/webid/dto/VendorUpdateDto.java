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
public class VendorUpdateDto {
    private String businessName;
    private String contactName;
    private String email;
    private String mobile;
    private String password; // New password if updating
    private List<Address> addresses;
    private List<LicenseDocument> licenseDocuments;
}

