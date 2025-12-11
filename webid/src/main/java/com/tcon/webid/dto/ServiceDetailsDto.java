package com.tcon.webid.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

/**
 * DTO for creating/updating service details
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceDetailsDto {

    private String id;
    private String vendorId;
    private String vendorOrganizationId;

    // Cuisine & Food Services
    private List<String> cuisineSpecialties;
    private List<String> dietaryOptions;

    // Service Types
    private List<String> serviceTypes;

    // Capacity & Coverage
    private Integer maximumCapacity;
    private Integer minimumCapacity;
    private List<String> serviceArea;

    // Pricing
    private Double startingPricePerPerson;
    private String pricingModel;

    // Special Services & Equipment
    private List<String> specialServices;
    private List<String> equipment;

    // Additional Information
    private String website;
    private Integer yearsInBusiness;
    private String aboutBusiness;
    private List<String> certifications;
    private List<String> portfolioImages;

    // Availability
    private Boolean availableForBooking;
    private Integer advanceBookingDays;

    // Metadata
    private String createdAt;
    private String updatedAt;
}

