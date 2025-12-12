package com.tcon.webid.controller;


import com.tcon.webid.dto.*;
import com.tcon.webid.service.VendorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.util.List;

@RestController
@RequestMapping("/api/vendor")
public class VendorController {

    @Autowired
    private VendorService vendorService;

    @PostMapping("/register")
    public ResponseEntity<?> registerVendor(@RequestBody VendorRegistrationDto dto) {
        String orgId = dto.getVendorOrganizationId();
        if (orgId == null || orgId.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("vendorOrganizationId must be provided by the client");
        }
        try {
            VendorResponseDto response = vendorService.registerVendor(dto);
            return ResponseEntity.ok(response);
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Registration failed: " + ex.getMessage());
        }
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

    /**
     * Update vendor details
     * PUT /api/vendor/{vendorId}
     */
    @PutMapping("/{vendorId}")
    public ResponseEntity<?> updateVendor(
            @PathVariable String vendorId,
            @RequestBody VendorUpdateDto dto) {
        try {
            VendorResponseDto response = vendorService.updateVendor(vendorId, dto);
            return ResponseEntity.ok(response);
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Update failed: " + ex.getMessage());
        }
    }
}
