package com.tcon.webid.service;

import com.tcon.webid.dto.NotificationRequestDto;
import com.tcon.webid.entity.Notification;
import java.util.List;

public interface NotificationService {
    Notification createNotification(NotificationRequestDto dto);
    List<Notification> getUserNotifications(String userId);
    List<Notification> getVendorNotifications(String vendorOrgId);
    void markAsRead(String notificationId);
}
