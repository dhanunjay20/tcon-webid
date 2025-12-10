package com.tcon.webid.service;

import com.tcon.webid.dto.BidUpdateNotification;
import com.tcon.webid.dto.ChatUpdateNotification;
import com.tcon.webid.dto.OrderUpdateNotification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * Service implementation for broadcasting real-time updates via WebSocket
 */
@Slf4j
@Service
public class RealTimeNotificationServiceImpl implements RealTimeNotificationService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Override
    public void broadcastBidUpdate(BidUpdateNotification notification) {
        try {
            log.info("Broadcasting bid update: {} for bid {}", notification.getEventType(), notification.getBidId());
            messagingTemplate.convertAndSend("/topic/bids", notification);
        } catch (Exception e) {
            log.error("Failed to broadcast bid update", e);
        }
    }

    @Override
    public void sendBidUpdateToUser(String userId, BidUpdateNotification notification) {
        try {
            log.info("Sending bid update to user {}: {} for bid {}", userId, notification.getEventType(), notification.getBidId());
            messagingTemplate.convertAndSendToUser(userId, "/queue/bids", notification);
        } catch (Exception e) {
            log.error("Failed to send bid update to user {}", userId, e);
        }
    }

    @Override
    public void sendBidUpdateToVendor(String vendorId, BidUpdateNotification notification) {
        try {
            log.info("Sending bid update to vendor {}: {} for bid {}", vendorId, notification.getEventType(), notification.getBidId());
            // Send to vendor-specific queue using MongoDB _id
            messagingTemplate.convertAndSend("/topic/vendor/" + vendorId + "/bids", notification);
        } catch (Exception e) {
            log.error("Failed to send bid update to vendor {}", vendorId, e);
        }
    }

    @Override
    public void broadcastOrderUpdate(OrderUpdateNotification notification) {
        try {
            log.info("Broadcasting order update: {} for order {}", notification.getEventType(), notification.getOrderId());
            messagingTemplate.convertAndSend("/topic/orders", notification);
        } catch (Exception e) {
            log.error("Failed to broadcast order update", e);
        }
    }

    @Override
    public void sendOrderUpdateToUser(String userId, OrderUpdateNotification notification) {
        try {
            log.info("Sending order update to user {}: {} for order {}", userId, notification.getEventType(), notification.getOrderId());
            messagingTemplate.convertAndSendToUser(userId, "/queue/orders", notification);
        } catch (Exception e) {
            log.error("Failed to send order update to user {}", userId, e);
        }
    }

    @Override
    public void sendOrderUpdateToVendor(String vendorId, OrderUpdateNotification notification) {
        try {
            log.info("Sending order update to vendor {}: {} for order {}", vendorId, notification.getEventType(), notification.getOrderId());
            // Send to vendor-specific queue using MongoDB _id
            messagingTemplate.convertAndSend("/topic/vendor/" + vendorId + "/orders", notification);
        } catch (Exception e) {
            log.error("Failed to send order update to vendor {}", vendorId, e);
        }
    }

    @Override
    public void broadcastChatUpdate(ChatUpdateNotification notification) {
        try {
            log.info("Broadcasting chat update: {} for message {}", notification.getEventType(), notification.getMessageId());
            messagingTemplate.convertAndSend("/topic/chats", notification);
        } catch (Exception e) {
            log.error("Failed to broadcast chat update", e);
        }
    }

    @Override
    public void sendChatUpdateToUser(String userId, ChatUpdateNotification notification) {
        try {
            log.info("Sending chat update to user {}: {} for message {}", userId, notification.getEventType(), notification.getMessageId());
            messagingTemplate.convertAndSendToUser(userId, "/queue/chats", notification);
        } catch (Exception e) {
            log.error("Failed to send chat update to user {}", userId, e);
        }
    }

    @Override
    public void sendChatUpdateToVendor(String vendorId, ChatUpdateNotification notification) {
        try {
            log.info("Sending chat update to vendor {}: {} for message {}", vendorId, notification.getEventType(), notification.getMessageId());
            // Send to vendor-specific queue using MongoDB _id
            messagingTemplate.convertAndSend("/topic/vendor/" + vendorId + "/chats", notification);
        } catch (Exception e) {
            log.error("Failed to send chat update to vendor {}", vendorId, e);
        }
    }
}

