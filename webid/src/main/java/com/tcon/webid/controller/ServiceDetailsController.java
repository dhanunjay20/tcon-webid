package com.tcon.webid.controller;

import com.tcon.webid.dto.ServiceDetailsDto;
import com.tcon.webid.service.ServiceDetailsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing vendor service details
 */
@Slf4j
@RestController
@RequestMapping("/api/service-details")
public class ServiceDetailsController {

    @Autowired
    private ServiceDetailsService serviceDetailsService;

    /**
     * Get all service details
     * GET /api/service-details
     */
    @GetMapping
    public ResponseEntity<List<ServiceDetailsDto>> getAllServiceDetails() {
        try {
            List<ServiceDetailsDto> results = serviceDetailsService.getAllServiceDetails();
            return ResponseEntity.ok(results);
        } catch (Exception ex) {
            log.error("Error fetching all service details: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Search by service type
     * GET /api/service-details/search/service-type?type={serviceType}
     */
    @GetMapping("/search/service-type")
    public ResponseEntity<List<ServiceDetailsDto>> searchByServiceType(
            @RequestParam String type) {
        try {
            List<ServiceDetailsDto> results = serviceDetailsService.searchByServiceType(type);
            return ResponseEntity.ok(results);
        } catch (Exception ex) {
            log.error("Error searching by service type: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Search by cuisine
     * GET /api/service-details/search/cuisine?cuisine={cuisine}
     */
    @GetMapping("/search/cuisine")
    public ResponseEntity<List<ServiceDetailsDto>> searchByCuisine(
            @RequestParam String cuisine) {
        try {
            List<ServiceDetailsDto> results = serviceDetailsService.searchByCuisine(cuisine);
            return ResponseEntity.ok(results);
        } catch (Exception ex) {
            log.error("Error searching by cuisine: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Search by service area
     * GET /api/service-details/search/area?area={area}
     */
    @GetMapping("/search/area")
    public ResponseEntity<List<ServiceDetailsDto>> searchByArea(
            @RequestParam String area) {
        try {
            List<ServiceDetailsDto> results = serviceDetailsService.searchByArea(area);
            return ResponseEntity.ok(results);
        } catch (Exception ex) {
            log.error("Error searching by service area: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Create or update service details for a vendor
     * POST /api/service-details/{vendorId}
     */
    @PostMapping("/{vendorId}")
    public ResponseEntity<?> createOrUpdateServiceDetails(
            @PathVariable String vendorId,
            @RequestBody ServiceDetailsDto dto) {
        try {
            log.info("Request to create/update service details for vendor: {}", vendorId);
            ServiceDetailsDto result = serviceDetailsService.createOrUpdateServiceDetails(vendorId, dto);
            return ResponseEntity.ok(result);
        } catch (RuntimeException ex) {
            log.error("Error creating/updating service details: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        } catch (Exception ex) {
            log.error("Unexpected error: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to create/update service details: " + ex.getMessage());
        }
    }

    /**
     * Update service details for a vendor (same as POST)
     * PUT /api/service-details/{vendorId}
     */
    @PutMapping("/{vendorId}")
    public ResponseEntity<?> updateServiceDetails(
            @PathVariable String vendorId,
            @RequestBody ServiceDetailsDto dto) {
        return createOrUpdateServiceDetails(vendorId, dto);
    }

    /**
     * Get service details by vendor ID
     * GET /api/service-details/vendor/{vendorId}
     * Returns empty DTO if not found (instead of 404) for better frontend integration
     */
    @GetMapping("/vendor/{vendorId}")
    public ResponseEntity<?> getServiceDetailsByVendorId(@PathVariable String vendorId) {
        try {
            ServiceDetailsDto result = serviceDetailsService.getServiceDetailsByVendorId(vendorId);
            if (result == null) {
                // Return empty DTO instead of 404 for new vendors
                log.info("No service details found for vendor: {}, returning empty DTO", vendorId);
                ServiceDetailsDto emptyDto = new ServiceDetailsDto();
                emptyDto.setVendorId(vendorId);
                return ResponseEntity.ok(emptyDto);
            }
            return ResponseEntity.ok(result);
        } catch (Exception ex) {
            log.error("Error fetching service details: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to fetch service details: " + ex.getMessage());
        }
    }

    /**
     * Get service details by vendor organization ID
     * GET /api/service-details/org/{vendorOrgId}
     */
    @GetMapping("/org/{vendorOrgId}")
    public ResponseEntity<?> getServiceDetailsByVendorOrgId(@PathVariable String vendorOrgId) {
        try {
            ServiceDetailsDto result = serviceDetailsService.getServiceDetailsByVendorOrgId(vendorOrgId);
            if (result == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Service details not found for vendor org: " + vendorOrgId);
            }
            return ResponseEntity.ok(result);
        } catch (Exception ex) {
            log.error("Error fetching service details: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to fetch service details: " + ex.getMessage());
        }
    }

    /**
     * Delete service details
     * DELETE /api/service-details/vendor/{vendorId}
     */
    @DeleteMapping("/vendor/{vendorId}")
    public ResponseEntity<?> deleteServiceDetails(@PathVariable String vendorId) {
        try {
            serviceDetailsService.deleteServiceDetails(vendorId);
            return ResponseEntity.ok("Service details deleted successfully");
        } catch (Exception ex) {
            log.error("Error deleting service details: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to delete service details: " + ex.getMessage());
        }
    }
}