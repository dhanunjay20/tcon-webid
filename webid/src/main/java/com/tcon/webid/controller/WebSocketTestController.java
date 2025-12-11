package com.tcon.webid.controller;

import com.tcon.webid.dto.BidUpdateNotification;
import com.tcon.webid.dto.ChatEventDto;
import com.tcon.webid.dto.OrderUpdateNotification;
import com.tcon.webid.service.RealTimeNotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Test controller for WebSocket real-time notifications
 * This is for testing purposes only - remove in production
 */
@Slf4j
@RestController
@RequestMapping("/api/test/notifications")
public class WebSocketTestController {

    @Autowired
    private RealTimeNotificationService realTimeNotificationService;

    /**
     * Test endpoint to send a sample bid notification
     */
    @PostMapping("/bid/broadcast")
    public ResponseEntity<String> testBidBroadcast() {
        log.info("Testing bid broadcast notification");

        BidUpdateNotification notification = BidUpdateNotification.builder()
                .bidId("test-bid-123")
                .orderId("test-order-456")
                .vendorOrganizationId("TEST_VENDOR_ORG")
                .status("quoted")
                .eventType("BID_QUOTED")
                .message("Test bid notification - vendor submitted a quote")
                .proposedTotalPrice(5000.0)
                .customerName("Test Customer")
                .vendorBusinessName("Test Vendor")
                .eventName("Test Event")
                .build();

        realTimeNotificationService.broadcastBidUpdate(notification);

        return ResponseEntity.ok("Bid notification broadcast successfully");
    }

    /**
     * Test endpoint to send a sample bid notification to a specific user
     */
    @PostMapping("/bid/user/{userId}")
    public ResponseEntity<String> testBidToUser(@PathVariable String userId) {
        log.info("Testing bid notification to user: {}", userId);

        BidUpdateNotification notification = BidUpdateNotification.builder()
                .bidId("test-bid-123")
                .orderId("test-order-456")
                .vendorOrganizationId("TEST_VENDOR_ORG")
                .status("quoted")
                .eventType("BID_QUOTED")
                .message("Test bid notification for user " + userId)
                .proposedTotalPrice(5000.0)
                .customerName("Test Customer")
                .vendorBusinessName("Test Vendor")
                .eventName("Test Event")
                .build();

        realTimeNotificationService.sendBidUpdateToUser(userId, notification);

        return ResponseEntity.ok("Bid notification sent to user: " + userId);
    }

    /**
     * Test endpoint to send a sample bid notification to a specific vendor
     */
    @PostMapping("/bid/vendor/{vendorOrgId}")
    public ResponseEntity<String> testBidToVendor(@PathVariable String vendorOrgId) {
        log.info("Testing bid notification to vendor: {}", vendorOrgId);

        BidUpdateNotification notification = BidUpdateNotification.builder()
                .bidId("test-bid-123")
                .orderId("test-order-456")
                .vendorOrganizationId(vendorOrgId)
                .status("requested")
                .eventType("BID_CREATED")
                .message("Test bid notification for vendor " + vendorOrgId)
                .proposedTotalPrice(0.0)
                .customerName("Test Customer")
                .vendorBusinessName("Test Vendor")
                .eventName("Test Event")
                .build();

        realTimeNotificationService.sendBidUpdateToVendor(vendorOrgId, notification);

        return ResponseEntity.ok("Bid notification sent to vendor: " + vendorOrgId);
    }

    /**
     * Test endpoint to send a sample order notification
     */
    @PostMapping("/order/broadcast")
    public ResponseEntity<String> testOrderBroadcast() {
        log.info("Testing order broadcast notification");

        OrderUpdateNotification notification = OrderUpdateNotification.builder()
                .orderId("test-order-456")
                .customerId("test-customer-123")
                .eventName("Test Event")
                .eventDate("2025-12-25")
                .eventLocation("Test Location")
                .guestCount(50)
                .status("pending")
                .totalPrice(10000.0)
                .eventType("ORDER_CREATED")
                .message("Test order notification - new order created")
                .build();

        realTimeNotificationService.broadcastOrderUpdate(notification);

        return ResponseEntity.ok("Order notification broadcast successfully");
    }

    /**
     * Test endpoint to send a sample order notification to a specific user
     */
    @PostMapping("/order/user/{userId}")
    public ResponseEntity<String> testOrderToUser(@PathVariable String userId) {
        log.info("Testing order notification to user: {}", userId);

        OrderUpdateNotification notification = OrderUpdateNotification.builder()
                .orderId("test-order-456")
                .customerId(userId)
                .eventName("Test Event")
                .eventDate("2025-12-25")
                .eventLocation("Test Location")
                .guestCount(50)
                .status("confirmed")
                .totalPrice(10000.0)
                .eventType("ORDER_STATUS_CHANGED")
                .message("Test order notification for user " + userId)
                .build();

        realTimeNotificationService.sendOrderUpdateToUser(userId, notification);

        return ResponseEntity.ok("Order notification sent to user: " + userId);
    }

    /**
     * Test endpoint to send a sample order notification to a specific vendor
     */
    @PostMapping("/order/vendor/{vendorOrgId}")
    public ResponseEntity<String> testOrderToVendor(@PathVariable String vendorOrgId) {
        log.info("Testing order notification to vendor: {}", vendorOrgId);

        OrderUpdateNotification notification = OrderUpdateNotification.builder()
                .orderId("test-order-456")
                .customerId("test-customer-123")
                .vendorOrganizationId(vendorOrgId)
                .eventName("Test Event")
                .eventDate("2025-12-25")
                .eventLocation("Test Location")
                .guestCount(50)
                .status("confirmed")
                .totalPrice(10000.0)
                .eventType("ORDER_STATUS_CHANGED")
                .message("Test order notification for vendor " + vendorOrgId)
                .build();

        realTimeNotificationService.sendOrderUpdateToVendor(vendorOrgId, notification);

        return ResponseEntity.ok("Order notification sent to vendor: " + vendorOrgId);
    }

    /**
     * Test endpoint to send a sample chat notification
     */
    @PostMapping("/chat/broadcast")
    public ResponseEntity<String> testChatBroadcast() {
        log.info("Testing chat broadcast notification");

        ChatEventDto event = ChatEventDto.builder()
                .eventType(ChatEventDto.EventType.MESSAGE_NEW)
                .messageId("test-msg-123")
                .chatId("user1_user2")
                .senderId("test-sender-123")
                .recipientId("test-recipient-456")
                .content("Test chat message")
                .messageStatus("SENT")
                .build();

        realTimeNotificationService.broadcastChatEvent(event);

        return ResponseEntity.ok("Chat notification broadcast successfully");
    }

    /**
     * Test endpoint to send a sample chat notification to a specific user
     */
    @PostMapping("/chat/user/{userId}")
    public ResponseEntity<String> testChatToUser(@PathVariable String userId) {
        log.info("Testing chat notification to user: {}", userId);

        ChatEventDto event = ChatEventDto.builder()
                .eventType(ChatEventDto.EventType.MESSAGE_NEW)
                .messageId("test-msg-123")
                .chatId("user1_user2")
                .senderId("test-sender-123")
                .recipientId(userId)
                .content("Test chat message for user")
                .messageStatus("SENT")
                .build();

        realTimeNotificationService.sendChatEventToUser(userId, event);

        return ResponseEntity.ok("Chat notification sent to user: " + userId);
    }

    /**
     * Test endpoint to send a sample chat notification to a specific vendor
     */
    @PostMapping("/chat/vendor/{vendorId}")
    public ResponseEntity<String> testChatToVendor(@PathVariable String vendorId) {
        log.info("Testing chat notification to vendor: {}", vendorId);

        ChatEventDto event = ChatEventDto.builder()
                .eventType(ChatEventDto.EventType.MESSAGE_NEW)
                .messageId("test-msg-123")
                .chatId("user1_vendor1")
                .senderId("test-sender-123")
                .recipientId(vendorId)
                .content("Test chat message for vendor")
                .messageStatus("SENT")
                .build();

        realTimeNotificationService.sendChatEventToVendor(vendorId, event);

        return ResponseEntity.ok("Chat notification sent to vendor: " + vendorId);
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("WebSocket notification test controller is running");
    }
}

