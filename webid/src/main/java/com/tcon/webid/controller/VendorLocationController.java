package com.tcon.webid.controller;

import com.tcon.webid.dto.VendorResponseDto;
import com.tcon.webid.service.LocationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for location-based vendor search
 */
@Slf4j
@RestController
@RequestMapping("/api/vendors/location")
public class VendorLocationController {

    @Autowired
    private LocationService locationService;

    /**
     * Find vendors within a specified radius from user's location
     *
     * @param userLatitude User's latitude
     * @param userLongitude User's longitude
     * @param radius Radius value
     * @param unit Unit of measurement: "km" or "miles"
     * @return List of vendors within the radius, sorted by distance
     */
    @GetMapping("/nearby")
    public ResponseEntity<List<VendorResponseDto>> findVendorsNearby(
            @RequestParam Double userLatitude,
            @RequestParam Double userLongitude,
            @RequestParam(defaultValue = "5") Double radius,
            @RequestParam(defaultValue = "km") String unit) {
        try {
            log.info("Finding vendors nearby - lat: {}, lon: {}, radius: {} {}",
                    userLatitude, userLongitude, radius, unit);

            // Validate coordinates
            if (userLatitude == null || userLongitude == null ||
                userLatitude < -90 || userLatitude > 90 ||
                userLongitude < -180 || userLongitude > 180) {
                log.warn("Invalid coordinates: lat={}, lon={}", userLatitude, userLongitude);
                return ResponseEntity.badRequest().build();
            }

            List<VendorResponseDto> vendors = locationService.findVendorsWithinRadius(
                    userLatitude, userLongitude, radius, unit);

            log.info("Found {} vendors nearby", vendors.size());
            return ResponseEntity.ok(vendors);
        } catch (Exception e) {
            log.error("Error finding vendors nearby: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Search vendors by business name and address
     *
     * @param searchTerm Search term to match against vendor name and address fields
     * @return List of matching vendors
     */
    @GetMapping("/search")
    public ResponseEntity<List<VendorResponseDto>> searchVendors(
            @RequestParam String searchTerm) {
        try {
            if (searchTerm == null || searchTerm.trim().isEmpty()) {
                log.warn("Empty search term provided");
                return ResponseEntity.badRequest().build();
            }

            log.info("Searching vendors for term: {}", searchTerm);

            List<VendorResponseDto> vendors = locationService.searchVendorsByNameAndAddress(searchTerm);

            log.info("Found {} vendors matching search term: {}", vendors.size(), searchTerm);
            return ResponseEntity.ok(vendors);
        } catch (Exception e) {
            log.error("Error searching vendors: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }
}
