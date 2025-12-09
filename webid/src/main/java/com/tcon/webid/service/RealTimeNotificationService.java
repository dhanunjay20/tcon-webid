package com.tcon.webid.service;

import com.tcon.webid.dto.BidUpdateNotification;
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
     * Send bid update to a specific vendor
     * @param vendorOrganizationId The vendor organization ID to send to
     * @param notification The bid update notification
     */
    void sendBidUpdateToVendor(String vendorOrganizationId, BidUpdateNotification notification);

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
     * Send order update to a specific vendor
     * @param vendorOrganizationId The vendor organization ID to send to
     * @param notification The order update notification
     */
    void sendOrderUpdateToVendor(String vendorOrganizationId, OrderUpdateNotification notification);
}


