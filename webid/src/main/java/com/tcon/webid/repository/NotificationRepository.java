package com.tcon.webid.repository;

import com.tcon.webid.entity.Notification;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface NotificationRepository extends MongoRepository<Notification, String> {
    List<Notification> findByRecipientUserIdOrderByCreatedAtDesc(String userId);
    List<Notification> findByRecipientVendorOrgIdOrderByCreatedAtDesc(String vendorOrgId);
}
