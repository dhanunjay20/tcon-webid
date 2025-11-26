package com.tcon.webid.repository;

import com.tcon.webid.entity.VendorReview;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface VendorReviewRepository extends MongoRepository<VendorReview, String> {
    List<VendorReview> findByVendorOrganizationId(String vendorOrganizationId);
}
