package com.tcon.webid.service.impl;

import com.tcon.webid.dto.NotificationRequestDto;
import com.tcon.webid.entity.Notification;
import com.tcon.webid.repository.NotificationRepository;
import com.tcon.webid.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationRepository repo;

    @Override
    public Notification createNotification(NotificationRequestDto dto) {
        Notification noti = new Notification();
        noti.setRecipientUserId(dto.getRecipientUserId());
        noti.setRecipientVendorOrgId(dto.getRecipientVendorOrgId());
        noti.setType(dto.getType());
        noti.setMessage(dto.getMessage());
        noti.setDataId(dto.getDataId());
        noti.setDataType(dto.getDataType());
        noti.setRead(false);
        noti.setCreatedAt(Instant.now().toString());
        return repo.save(noti);
    }

    @Override
    public List<Notification> getUserNotifications(String userId) {
        return repo.findByRecipientUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    public List<Notification> getVendorNotifications(String vendorOrgId) {
        return repo.findByRecipientVendorOrgIdOrderByCreatedAtDesc(vendorOrgId);
    }

    @Override
    public void markAsRead(String notificationId) {
        Notification n = repo.findById(notificationId).orElseThrow(() -> new RuntimeException("Not found"));
        n.setRead(true);
        repo.save(n);
    }
}
