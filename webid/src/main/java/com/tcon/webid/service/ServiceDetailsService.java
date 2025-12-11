package com.tcon.webid.service;

import com.tcon.webid.dto.ServiceDetailsDto;
import com.tcon.webid.entity.ServiceDetails;
import com.tcon.webid.entity.Vendor;
import com.tcon.webid.repository.ServiceDetailsRepository;
import com.tcon.webid.repository.VendorRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ServiceDetailsService {

    @Autowired
    private ServiceDetailsRepository serviceDetailsRepository;

    @Autowired
    private VendorRepository vendorRepository;

    /**
     * Create or update service details for a vendor
     */
    public ServiceDetailsDto createOrUpdateServiceDetails(String vendorId, ServiceDetailsDto dto) {
        log.info("Creating/Updating service details for vendor: {}", vendorId);

        // Verify vendor exists
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new RuntimeException("Vendor not found with ID: " + vendorId));

        ServiceDetails serviceDetails;
        Optional<ServiceDetails> existingOpt = serviceDetailsRepository.findByVendorId(vendorId);

        if (existingOpt.isPresent()) {
            serviceDetails = existingOpt.get();
            serviceDetails.setUpdatedAt(Instant.now().toString());
            log.info("Updating existing service details for vendor: {}", vendorId);
        } else {
            serviceDetails = new ServiceDetails();
            serviceDetails.setVendorId(vendorId);
            serviceDetails.setVendorOrganizationId(vendor.getVendorOrganizationId());
            serviceDetails.setCreatedAt(Instant.now().toString());
            serviceDetails.setUpdatedAt(Instant.now().toString());
            log.info("Creating new service details for vendor: {}", vendorId);
        }

        // Update fields from DTO
        if (dto.getCuisineSpecialties() != null) serviceDetails.setCuisineSpecialties(dto.getCuisineSpecialties());
        if (dto.getDietaryOptions() != null) serviceDetails.setDietaryOptions(dto.getDietaryOptions());
        if (dto.getServiceTypes() != null) serviceDetails.setServiceTypes(dto.getServiceTypes());
        if (dto.getMaximumCapacity() != null) serviceDetails.setMaximumCapacity(dto.getMaximumCapacity());
        if (dto.getMinimumCapacity() != null) serviceDetails.setMinimumCapacity(dto.getMinimumCapacity());
        if (dto.getServiceArea() != null) serviceDetails.setServiceArea(dto.getServiceArea());
        if (dto.getStartingPricePerPerson() != null) serviceDetails.setStartingPricePerPerson(dto.getStartingPricePerPerson());
        if (dto.getPricingModel() != null) serviceDetails.setPricingModel(dto.getPricingModel());
        if (dto.getSpecialServices() != null) serviceDetails.setSpecialServices(dto.getSpecialServices());
        if (dto.getEquipment() != null) serviceDetails.setEquipment(dto.getEquipment());
        if (dto.getWebsite() != null) serviceDetails.setWebsite(dto.getWebsite());
        if (dto.getYearsInBusiness() != null) serviceDetails.setYearsInBusiness(dto.getYearsInBusiness());
        if (dto.getAboutBusiness() != null) serviceDetails.setAboutBusiness(dto.getAboutBusiness());
        if (dto.getCertifications() != null) serviceDetails.setCertifications(dto.getCertifications());
        if (dto.getPortfolioImages() != null) serviceDetails.setPortfolioImages(dto.getPortfolioImages());
        if (dto.getAvailableForBooking() != null) serviceDetails.setAvailableForBooking(dto.getAvailableForBooking());
        if (dto.getAdvanceBookingDays() != null) serviceDetails.setAdvanceBookingDays(dto.getAdvanceBookingDays());

        ServiceDetails saved = serviceDetailsRepository.save(serviceDetails);
        return toDto(saved);
    }

    /**
     * Get service details by vendor ID
     * Returns DTO with vendorId and vendorOrganizationId populated if vendor exists but no service details
     */
    public ServiceDetailsDto getServiceDetailsByVendorId(String vendorId) {
        log.info("Fetching service details for vendor: {}", vendorId);

        Optional<ServiceDetails> serviceDetailsOpt = serviceDetailsRepository.findByVendorId(vendorId);

        if (serviceDetailsOpt.isPresent()) {
            return toDto(serviceDetailsOpt.get());
        }

        // If no service details exist, create empty DTO with vendor info
        log.info("No service details found for vendor: {}, creating empty DTO", vendorId);

        // Try to get vendor to populate organization ID
        Optional<Vendor> vendorOpt = vendorRepository.findById(vendorId);
        if (vendorOpt.isPresent()) {
            ServiceDetailsDto emptyDto = new ServiceDetailsDto();
            emptyDto.setVendorId(vendorId);
            emptyDto.setVendorOrganizationId(vendorOpt.get().getVendorOrganizationId());
            emptyDto.setAvailableForBooking(true); // Default to available
            return emptyDto;
        }

        return null; // Vendor doesn't exist
    }

    /**
     * Get service details by vendor organization ID
     */
    public ServiceDetailsDto getServiceDetailsByVendorOrgId(String vendorOrgId) {
        log.info("Fetching service details for vendor org: {}", vendorOrgId);
        return serviceDetailsRepository.findByVendorOrganizationId(vendorOrgId)
                .map(this::toDto)
                .orElse(null);
    }

    /**
     * Get all service details
     */
    public List<ServiceDetailsDto> getAllServiceDetails() {
        return serviceDetailsRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Search by service type
     */
    public List<ServiceDetailsDto> searchByServiceType(String serviceType) {
        return serviceDetailsRepository.findByServiceTypesContaining(serviceType).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Search by cuisine
     */
    public List<ServiceDetailsDto> searchByCuisine(String cuisine) {
        return serviceDetailsRepository.findByCuisineSpecialtiesContaining(cuisine).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Search by service area
     */
    public List<ServiceDetailsDto> searchByArea(String area) {
        return serviceDetailsRepository.findByServiceAreaContaining(area).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Delete service details
     */
    public void deleteServiceDetails(String vendorId) {
        log.info("Deleting service details for vendor: {}", vendorId);
        serviceDetailsRepository.findByVendorId(vendorId)
                .ifPresent(serviceDetails -> serviceDetailsRepository.deleteById(serviceDetails.getId()));
    }

    /**
     * Convert entity to DTO
     */
    private ServiceDetailsDto toDto(ServiceDetails entity) {
        ServiceDetailsDto dto = new ServiceDetailsDto();
        dto.setId(entity.getId());
        dto.setVendorId(entity.getVendorId());
        dto.setVendorOrganizationId(entity.getVendorOrganizationId());
        dto.setCuisineSpecialties(entity.getCuisineSpecialties());
        dto.setDietaryOptions(entity.getDietaryOptions());
        dto.setServiceTypes(entity.getServiceTypes());
        dto.setMaximumCapacity(entity.getMaximumCapacity());
        dto.setMinimumCapacity(entity.getMinimumCapacity());
        dto.setServiceArea(entity.getServiceArea());
        dto.setStartingPricePerPerson(entity.getStartingPricePerPerson());
        dto.setPricingModel(entity.getPricingModel());
        dto.setSpecialServices(entity.getSpecialServices());
        dto.setEquipment(entity.getEquipment());
        dto.setWebsite(entity.getWebsite());
        dto.setYearsInBusiness(entity.getYearsInBusiness());
        dto.setAboutBusiness(entity.getAboutBusiness());
        dto.setCertifications(entity.getCertifications());
        dto.setPortfolioImages(entity.getPortfolioImages());
        dto.setAvailableForBooking(entity.getAvailableForBooking());
        dto.setAdvanceBookingDays(entity.getAdvanceBookingDays());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }
}

