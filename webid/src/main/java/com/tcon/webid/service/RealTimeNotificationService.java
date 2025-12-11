package com.tcon.webid.service;

import com.tcon.webid.dto.BidUpdateNotification;
import com.tcon.webid.dto.ChatEventDto;
import com.tcon.webid.dto.OrderUpdateNotification;

/**
 * Service for broadcasting real-time updates via WebSocket
 */
public interface RealTimeNotificationService {

    /**
     * Broadcast bid update to all subscribed users
     * @param notification The bid update notification
     */
    void broadcastBidUpdate(BidUpdateNotification notification);

    /**
     * Send bid update to a specific user
     * @param userId The user ID to send to
     * @param notification The bid update notification
     */
    void sendBidUpdateToUser(String userId, BidUpdateNotification notification);

    /**
     * Send bid update to a specific vendor using MongoDB _id
     * @param vendorId The vendor MongoDB _id to send to
     * @param notification The bid update notification
     */
    void sendBidUpdateToVendor(String vendorId, BidUpdateNotification notification);

    /**
     * Broadcast order update to all subscribed users
     * @param notification The order update notification
     */
    void broadcastOrderUpdate(OrderUpdateNotification notification);

    /**
     * Send order update to a specific user
     * @param userId The user ID to send to
     * @param notification The order update notification
     */
    void sendOrderUpdateToUser(String userId, OrderUpdateNotification notification);

    /**
     * Send order update to a specific vendor using MongoDB _id
     * @param vendorId The vendor MongoDB _id to send to
     * @param notification The order update notification
     */
    void sendOrderUpdateToVendor(String vendorId, OrderUpdateNotification notification);

    /**
     * Broadcast chat event to all subscribed users
     * @param event The chat event
     */
    void broadcastChatEvent(ChatEventDto event);

    /**
     * Send chat event to a specific user
     * @param userId The user ID to send to
     * @param event The chat event
     */
    void sendChatEventToUser(String userId, ChatEventDto event);

    /**
     * Send chat event to a specific vendor using MongoDB _id
     * @param vendorId The vendor MongoDB _id to send to
     * @param event The chat event
     */
    void sendChatEventToVendor(String vendorId, ChatEventDto event);
}




