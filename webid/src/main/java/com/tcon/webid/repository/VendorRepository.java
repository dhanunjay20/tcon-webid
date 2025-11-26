package com.tcon.webid.repository;

import com.tcon.webid.entity.Vendor;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;
import java.util.List;

public interface VendorRepository extends MongoRepository<Vendor, String> {
    Optional<Vendor> findByEmail(String email);
    Optional<Vendor> findByMobile(String mobile);
    Optional<Vendor> findByMobileIn(List<String> mobiles);
    boolean existsByEmail(String email);
    boolean existsByMobile(String mobile);
    boolean existsByVendorOrganizationId(String vendorOrganizationId);
    Optional<Vendor> findByVendorOrganizationId(String vendorOrganizationId);
}
