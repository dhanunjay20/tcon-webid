package com.tcon.webid.service;

import com.tcon.webid.dto.BidUpdateNotification;
import com.tcon.webid.dto.ChatEventDto;
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
    public void broadcastChatEvent(ChatEventDto event) {
        try {
            log.info("Broadcasting chat event: {} for chat {}", event.getEventType(), event.getChatId());
            messagingTemplate.convertAndSend("/topic/chat-events", event);
        } catch (Exception e) {
            log.error("Failed to broadcast chat event", e);
        }
    }

    @Override
    public void sendChatEventToUser(String userId, ChatEventDto event) {
        try {
            log.info("Sending chat event to user {}: {}", userId, event.getEventType());
            messagingTemplate.convertAndSendToUser(userId, "/queue/chat-events", event);
        } catch (Exception e) {
            log.error("Failed to send chat event to user {}", userId, e);
        }
    }

    @Override
    public void sendChatEventToVendor(String vendorId, ChatEventDto event) {
        try {
            log.info("Sending chat event to vendor {}: {}", vendorId, event.getEventType());
            // Send to vendor-specific queue using MongoDB _id
            messagingTemplate.convertAndSend("/topic/vendor/" + vendorId + "/chat-events", event);
        } catch (Exception e) {
            log.error("Failed to send chat event to vendor {}", vendorId, e);
        }
    }
}

