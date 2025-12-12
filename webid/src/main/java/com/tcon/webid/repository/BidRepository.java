package com.tcon.webid.repository;

import com.tcon.webid.entity.Bid;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface BidRepository extends MongoRepository<Bid, String> {
    List<Bid> findByOrderId(String orderId);
    List<Bid> findByVendorOrganizationId(String vendorOrganizationId);
}
