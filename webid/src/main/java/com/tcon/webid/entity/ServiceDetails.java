package com.tcon.webid.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

/**
 * Entity representing detailed service information for a vendor
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "service_details")
public class ServiceDetails {

    @Id
    private String id;

    @Indexed(unique = true)
    private String vendorId; // Reference to Vendor MongoDB ID

    @Indexed
    private String vendorOrganizationId; // Reference to Vendor Organization ID

    // Cuisine & Food Services
    private List<String> cuisineSpecialties; // e.g., ["Indian", "Chinese", "Continental", "Italian"]
    private List<String> dietaryOptions; // e.g., ["Vegetarian", "Vegan", "Gluten-Free", "Halal", "Kosher"]

    // Service Types
    private List<String> serviceTypes; // e.g., ["Wedding Catering", "Corporate Events", "Birthday Parties", "Buffet Service"]

    // Capacity & Coverage
    private Integer maximumCapacity; // Maximum number of guests they can serve
    private Integer minimumCapacity; // Minimum number of guests required
    private List<String> serviceArea; // e.g., ["Mumbai", "Navi Mumbai", "Thane"]

    // Pricing
    private Double startingPricePerPerson; // Starting price per person
    private String pricingModel; // e.g., "Per Person", "Per Plate", "Fixed Package"

    // Special Services & Equipment
    private List<String> specialServices; // e.g., ["Live Cooking Stations", "Bartending", "Waitstaff", "Setup & Cleanup"]
    private List<String> equipment; // e.g., ["Tables", "Chairs", "Tents", "Sound System", "Lighting"]

    // Additional Information
    private String website;
    private Integer yearsInBusiness;
    private String aboutBusiness; // Detailed description
    private List<String> certifications; // e.g., ["FSSAI", "ISO Certified", "Health Department Approved"]
    private List<String> portfolioImages; // URLs to portfolio images

    // Availability
    private Boolean availableForBooking; // Current availability status
    private Integer advanceBookingDays; // Minimum days in advance required for booking

    // Metadata
    private String createdAt;
    private String updatedAt;
}

