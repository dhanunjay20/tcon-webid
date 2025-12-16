package com.tcon.webid.service;

import com.tcon.webid.dto.VendorResponseDto;
import com.tcon.webid.entity.Vendor;
import com.tcon.webid.repository.VendorRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for location-based vendor search and filtering
 */
@Slf4j
@Service
public class LocationService {

    @Autowired
    private VendorRepository vendorRepository;

    @Autowired
    private VendorService vendorService;

    /**
     * Calculate distance between two coordinates using Haversine formula
     * Returns distance in kilometers
     */
    public double calculateDistanceKm(double lat1, double lon1, double lat2, double lon2) {
        final int EARTH_RADIUS_KM = 6371;

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }

    /**
     * Convert kilometers to miles
     */
    public double kmToMiles(double km) {
        return km * 0.621371;
    }

    /**
     * Find vendors within a specified radius from user's location
     *
     * @param userLatitude User's latitude
     * @param userLongitude User's longitude
     * @param radius Radius in km or miles
     * @param unit "km" or "miles"
     * @return List of vendors within the radius with calculated distance
     */
    public List<VendorResponseDto> findVendorsWithinRadius(double userLatitude, double userLongitude,
                                                            double radius, String unit) {
        try {
            log.info("Finding vendors within {} {} from user location ({}, {})",
                    radius, unit, userLatitude, userLongitude);

            // Validate and normalize unit
            String normalizedUnit = unit;
            if (!normalizedUnit.equalsIgnoreCase("km") && !normalizedUnit.equalsIgnoreCase("miles")) {
                log.warn("Invalid unit: {}. Defaulting to km", unit);
                normalizedUnit = "km";
            }

            // Fetch all vendors with location data
            List<Vendor> allVendors = vendorRepository.findAll();

            // Convert radius to km if provided in miles
            double radiusKm = normalizedUnit.equalsIgnoreCase("miles") ? radius / 0.621371 : radius;

            // Capture final reference for lambda
            final String finalUnit = normalizedUnit;

            // Filter vendors within radius
            List<VendorResponseDto> vendorsInRadius = allVendors.stream()
                    .filter(vendor -> vendor.getLatitude() != null && vendor.getLongitude() != null)
                    .filter(vendor -> {
                        double distanceKm = calculateDistanceKm(userLatitude, userLongitude,
                                vendor.getLatitude(), vendor.getLongitude());
                        return distanceKm <= radiusKm;
                    })
                    .map(vendor -> {
                        VendorResponseDto dto = vendorService.mapVendorToDto(vendor);
                        // Add calculated distance to DTO
                        double distanceKm = calculateDistanceKm(userLatitude, userLongitude,
                                vendor.getLatitude(), vendor.getLongitude());
                        if (finalUnit.equalsIgnoreCase("miles")) {
                            dto.setDistance(kmToMiles(distanceKm));
                        } else {
                            dto.setDistance(distanceKm);
                        }
                        return dto;
                    })
                    .sorted((v1, v2) -> Double.compare(v1.getDistance(), v2.getDistance()))
                    .collect(Collectors.toList());

            log.info("Found {} vendors within {} {}", vendorsInRadius.size(), radius, normalizedUnit);
            return vendorsInRadius;
        } catch (Exception e) {
            log.error("Error finding vendors within radius: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Search vendors by business name and address (partial match)
     *
     * @param searchTerm Search term to match against business name and address fields
     * @return List of matching vendors
     */
    public List<VendorResponseDto> searchVendorsByNameAndAddress(String searchTerm) {
        try {
            if (searchTerm == null || searchTerm.trim().isEmpty()) {
                log.warn("Search term is empty");
                return new ArrayList<>();
            }

            String lowerSearchTerm = searchTerm.trim().toLowerCase();
            log.info("Searching vendors by name and address for term: {}", searchTerm);

            // Fetch all vendors and filter by name/address match
            List<Vendor> allVendors = vendorRepository.findAll();

            List<VendorResponseDto> matchingVendors = allVendors.stream()
                    .filter(vendor -> {
                        // Match against business name
                        boolean nameMatch = vendor.getBusinessName() != null &&
                                vendor.getBusinessName().toLowerCase().contains(lowerSearchTerm);

                        // Match against address fields
                        boolean addressMatch = false;
                        if (vendor.getAddresses() != null && !vendor.getAddresses().isEmpty()) {
                            addressMatch = vendor.getAddresses().stream()
                                    .anyMatch(addr ->
                                            (addr.getAddressLine1() != null && addr.getAddressLine1().toLowerCase().contains(lowerSearchTerm)) ||
                                            (addr.getAddressLine2() != null && addr.getAddressLine2().toLowerCase().contains(lowerSearchTerm)) ||
                                            (addr.getCity() != null && addr.getCity().toLowerCase().contains(lowerSearchTerm)) ||
                                            (addr.getState() != null && addr.getState().toLowerCase().contains(lowerSearchTerm)) ||
                                            (addr.getCountry() != null && addr.getCountry().toLowerCase().contains(lowerSearchTerm)) ||
                                            (addr.getZipCode() != null && addr.getZipCode().toLowerCase().contains(lowerSearchTerm))
                                    );
                        }

                        return nameMatch || addressMatch;
                    })
                    .map(vendorService::mapVendorToDto)
                    .collect(Collectors.toList());

            log.info("Found {} vendors matching search term: {}", matchingVendors.size(), searchTerm);
            return matchingVendors;
        } catch (Exception e) {
            log.error("Error searching vendors by name and address: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }
}
