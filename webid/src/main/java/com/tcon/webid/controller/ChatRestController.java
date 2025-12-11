package com.tcon.webid.controller;

import com.tcon.webid.dto.*;
import com.tcon.webid.service.ChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for chat operations.
 * Provides HTTP endpoints for:
 * - Fetching chat history
 * - Getting chat list
 * - Managing messages
 * - Getting unread counts
 *
 * These endpoints complement the WebSocket real-time functionality
 * and are used for initial data loading.
 */
@Slf4j
@RestController
@RequestMapping("/api/chat")
public class ChatRestController {

    @Autowired
    private ChatService chatService;

    // ==================== MESSAGE ENDPOINTS ====================

    /**
     * Send a new message (REST fallback for WebSocket)
     * POST /api/chat/messages
     */
    @PostMapping("/messages")
    public ResponseEntity<ChatMessageResponseDto> sendMessage(
            @RequestHeader("X-User-Id") String senderId,
            @RequestHeader(value = "X-User-Type", defaultValue = "USER") String senderType,
            @RequestBody ChatMessageRequestDto request) {
        try {
            log.info("REST: Sending message from {} to {}", senderId, request.getRecipientId());
            ChatMessageResponseDto response = chatService.sendMessage(senderId, senderType, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error sending message: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get chat history between two users
     * GET /api/chat/messages/{otherParticipantId}
     */
    @GetMapping("/messages/{otherParticipantId}")
    public ResponseEntity<List<ChatMessageResponseDto>> getChatHistory(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String otherParticipantId) {
        try {
            log.info("Fetching chat history for {} with {}", userId, otherParticipantId);
            List<ChatMessageResponseDto> messages = chatService.getChatHistory(userId, otherParticipantId);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            log.error("Error fetching chat history: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get chat history with pagination
     * GET /api/chat/messages/{otherParticipantId}/paginated
     */
    @GetMapping("/messages/{otherParticipantId}/paginated")
    public ResponseEntity<Page<ChatMessageResponseDto>> getChatHistoryPaginated(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String otherParticipantId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        try {
            log.info("Fetching paginated chat history for {} with {} (page: {}, size: {})",
                    userId, otherParticipantId, page, size);
            Page<ChatMessageResponseDto> messages =
                    chatService.getChatHistoryPaginated(userId, otherParticipantId, page, size);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            log.error("Error fetching paginated chat history: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Mark messages as read in a specific chat
     * PUT /api/chat/messages/read/{otherParticipantId}
     */
    @PutMapping("/messages/read/{otherParticipantId}")
    public ResponseEntity<Map<String, Integer>> markMessagesAsRead(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String otherParticipantId) {
        try {
            String chatId = chatService.getChatId(userId, otherParticipantId);
            log.info("Marking messages as read in chat {} for user {}", chatId, userId);
            int count = chatService.markMessagesAsRead(chatId, userId);
            return ResponseEntity.ok(Map.of("markedAsRead", count));
        } catch (Exception e) {
            log.error("Error marking messages as read: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Mark all pending messages as delivered (when user comes online)
     * PUT /api/chat/messages/delivered
     */
    @PutMapping("/messages/delivered")
    public ResponseEntity<Map<String, Integer>> markMessagesAsDelivered(
            @RequestHeader("X-User-Id") String userId) {
        try {
            log.info("Marking messages as delivered for user {}", userId);
            int count = chatService.markMessagesAsDelivered(userId);
            return ResponseEntity.ok(Map.of("markedAsDelivered", count));
        } catch (Exception e) {
            log.error("Error marking messages as delivered: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==================== CHAT LIST ENDPOINTS ====================

    /**
     * Get chat list (inbox) for a user
     * GET /api/chat/list
     */
    @GetMapping("/list")
    public ResponseEntity<List<ChatListItemDto>> getChatList(
            @RequestHeader("X-User-Id") String userId) {
        try {
            log.info("Fetching chat list for user {}", userId);
            List<ChatListItemDto> chatList = chatService.getChatList(userId);
            return ResponseEntity.ok(chatList);
        } catch (Exception e) {
            log.error("Error fetching chat list: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get unread message count
     * GET /api/chat/unread-count
     */
    @GetMapping("/unread-count")
    public ResponseEntity<UnreadCountDto> getUnreadCount(
            @RequestHeader("X-User-Id") String userId) {
        try {
            log.info("Fetching unread count for user {}", userId);
            UnreadCountDto unreadCount = chatService.getUnreadCount(userId);
            return ResponseEntity.ok(unreadCount);
        } catch (Exception e) {
            log.error("Error fetching unread count: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==================== PRESENCE ENDPOINTS ====================

    /**
     * Update online status
     * PUT /api/chat/presence/online
     */
    @PutMapping("/presence/online")
    public ResponseEntity<Void> goOnline(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader(value = "X-User-Type", defaultValue = "USER") String userType) {
        try {
            log.info("User {} ({}) going online", userId, userType);
            chatService.updateOnlineStatus(userId, userType, true);
            chatService.markMessagesAsDelivered(userId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error updating online status: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Update offline status
     * PUT /api/chat/presence/offline
     */
    @PutMapping("/presence/offline")
    public ResponseEntity<Void> goOffline(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader(value = "X-User-Type", defaultValue = "USER") String userType) {
        try {
            log.info("User {} ({}) going offline", userId, userType);
            chatService.updateOnlineStatus(userId, userType, false);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error updating offline status: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get user presence status
     * GET /api/chat/presence/{userId}
     */
    @GetMapping("/presence/{userId}")
    public ResponseEntity<UserPresenceDto> getUserPresence(@PathVariable String userId) {
        try {
            log.info("Fetching presence for user {}", userId);
            UserPresenceDto presence = chatService.getUserPresence(userId);
            return ResponseEntity.ok(presence);
        } catch (Exception e) {
            log.error("Error fetching user presence: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==================== UTILITY ENDPOINTS ====================

    /**
     * Get chat ID for two participants
     * GET /api/chat/id/{otherParticipantId}
     */
    @GetMapping("/id/{otherParticipantId}")
    public ResponseEntity<Map<String, String>> getChatId(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String otherParticipantId) {
        try {
            String chatId = chatService.getChatId(userId, otherParticipantId);
            return ResponseEntity.ok(Map.of("chatId", chatId));
        } catch (Exception e) {
            log.error("Error getting chat ID: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Delete a chat (soft delete)
     * DELETE /api/chat/{otherParticipantId}
     */
    @DeleteMapping("/{otherParticipantId}")
    public ResponseEntity<Void> deleteChat(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String otherParticipantId) {
        try {
            log.info("Deleting chat for user {} with {}", userId, otherParticipantId);
            chatService.deleteChat(userId, otherParticipantId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error deleting chat: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==================== VENDOR-SPECIFIC ENDPOINTS ====================

    /**
     * Get chat list for a vendor
     * GET /api/chat/vendor/list
     */
    @GetMapping("/vendor/list")
    public ResponseEntity<List<ChatListItemDto>> getVendorChatList(
            @RequestHeader("X-Vendor-Id") String vendorId) {
        try {
            log.info("Fetching chat list for vendor {}", vendorId);
            List<ChatListItemDto> chatList = chatService.getChatList(vendorId);
            return ResponseEntity.ok(chatList);
        } catch (Exception e) {
            log.error("Error fetching vendor chat list: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get chat history for a vendor
     * GET /api/chat/vendor/messages/{clientId}
     */
    @GetMapping("/vendor/messages/{clientId}")
    public ResponseEntity<List<ChatMessageResponseDto>> getVendorChatHistory(
            @RequestHeader("X-Vendor-Id") String vendorId,
            @PathVariable String clientId) {
        try {
            log.info("Fetching chat history for vendor {} with client {}", vendorId, clientId);
            List<ChatMessageResponseDto> messages = chatService.getChatHistory(vendorId, clientId);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            log.error("Error fetching vendor chat history: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Send message from vendor
     * POST /api/chat/vendor/messages
     */
    @PostMapping("/vendor/messages")
    public ResponseEntity<ChatMessageResponseDto> sendVendorMessage(
            @RequestHeader("X-Vendor-Id") String vendorId,
            @RequestBody ChatMessageRequestDto request) {
        try {
            log.info("Vendor {} sending message to {}", vendorId, request.getRecipientId());
            ChatMessageResponseDto response = chatService.sendMessage(vendorId, "VENDOR", request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error sending vendor message: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get unread count for vendor
     * GET /api/chat/vendor/unread-count
     */
    @GetMapping("/vendor/unread-count")
    public ResponseEntity<UnreadCountDto> getVendorUnreadCount(
            @RequestHeader("X-Vendor-Id") String vendorId) {
        try {
            log.info("Fetching unread count for vendor {}", vendorId);
            UnreadCountDto unreadCount = chatService.getUnreadCount(vendorId);
            return ResponseEntity.ok(unreadCount);
        } catch (Exception e) {
            log.error("Error fetching vendor unread count: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

