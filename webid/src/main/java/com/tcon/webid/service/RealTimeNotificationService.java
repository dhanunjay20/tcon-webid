package com.tcon.webid.service;

import com.tcon.webid.dto.BidUpdateNotification;
import com.tcon.webid.dto.ChatUpdateNotification;
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
     * Broadcast chat update to all subscribed users
     * @param notification The chat update notification
     */
    void broadcastChatUpdate(ChatUpdateNotification notification);

    /**
     * Send chat update to a specific user
     * @param userId The user ID to send to
     * @param notification The chat update notification
     */
    void sendChatUpdateToUser(String userId, ChatUpdateNotification notification);

    /**
     * Send chat update to a specific vendor using MongoDB _id
     * @param vendorId The vendor MongoDB _id to send to
     * @param notification The chat update notification
     */
    void sendChatUpdateToVendor(String vendorId, ChatUpdateNotification notification);
}




