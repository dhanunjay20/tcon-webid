package com.tcon.webid.repository;

import com.tcon.webid.entity.MenuItem;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface MenuItemRepository extends MongoRepository<MenuItem, String> {
    List<MenuItem> findByVendorOrganizationId(String vendorOrganizationId);
}
