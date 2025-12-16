package com.tcon.webid.service;

import com.tcon.webid.dto.*;
import com.tcon.webid.entity.Vendor;
import java.util.List;

public interface VendorService {
    VendorResponseDto registerVendor(VendorRegistrationDto dto);
    VendorResponseDto loginVendor(String login, String password);
    VendorResponseDto getVendorById(String id);
    List<VendorResponseDto> getAllVendors();
    VendorResponseDto updateVendor(String id, VendorUpdateDto dto);
    VendorResponseDto getVendorByOrganizationId(String vendorOrganizationId);
    VendorResponseDto mapVendorToDto(Vendor vendor);
}