package com.tcon.webid.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VendorLoginDto {
    private String login; // can be vendorOrganizationId, email, or mobile
    private String password;
}

