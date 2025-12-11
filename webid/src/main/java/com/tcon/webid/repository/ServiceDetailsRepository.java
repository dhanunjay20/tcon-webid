package com.tcon.webid.repository;

import com.tcon.webid.entity.ServiceDetails;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;
import java.util.List;

public interface ServiceDetailsRepository extends MongoRepository<ServiceDetails, String> {
    Optional<ServiceDetails> findByVendorId(String vendorId);
    Optional<ServiceDetails> findByVendorOrganizationId(String vendorOrganizationId);
    List<ServiceDetails> findByServiceTypesContaining(String serviceType);
    List<ServiceDetails> findByCuisineSpecialtiesContaining(String cuisine);
    List<ServiceDetails> findByServiceAreaContaining(String area);
    List<ServiceDetails> findByAvailableForBooking(Boolean available);
}

