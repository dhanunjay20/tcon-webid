package com.tcon.webid.controller;

import com.tcon.webid.dto.NotificationRequestDto;
import com.tcon.webid.entity.Notification;
import com.tcon.webid.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService service;

    @PostMapping
    public Notification createNotification(@RequestBody NotificationRequestDto dto) {
        return service.createNotification(dto);
    }

    @GetMapping("/user/{userId}")
    public List<Notification> getUserNotifications(@PathVariable String userId) {
        return service.getUserNotifications(userId);
    }

    @GetMapping("/vendor/{vendorOrgId}")
    public List<Notification> getVendorNotifications(@PathVariable String vendorOrgId) {
        return service.getVendorNotifications(vendorOrgId);
    }

    @PutMapping("/{notificationId}/read")
    public void markAsRead(@PathVariable String notificationId) {
        service.markAsRead(notificationId);
    }
}
