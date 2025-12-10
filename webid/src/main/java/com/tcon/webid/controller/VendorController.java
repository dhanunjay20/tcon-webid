package com.tcon.webid.controller;


import com.tcon.webid.dto.*;
import com.tcon.webid.service.VendorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vendor")
public class VendorController {

    @Autowired
    private VendorService vendorService;

    @PostMapping("/register")
    public VendorResponseDto registerVendor(@RequestBody VendorRegistrationDto dto) {
        return vendorService.registerVendor(dto);
    }

    @PostMapping("/login")
    public VendorResponseDto loginVendor(@RequestBody VendorLoginDto dto) {
        return vendorService.loginVendor(dto.getLogin(), dto.getPassword());
    }

    @GetMapping("/{id}")
    public VendorResponseDto getVendor(@PathVariable String id) {
        return vendorService.getVendorById(id);
    }

    @GetMapping
    public List<VendorResponseDto> getAllVendors() {
        return vendorService.getAllVendors();
    }

    @GetMapping("/org/{vendorOrganizationId}")
    public VendorResponseDto getVendorByOrganizationId(@PathVariable String vendorOrganizationId) {
        return vendorService.getVendorByOrganizationId(vendorOrganizationId);
    }
}
