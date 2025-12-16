package com.tcon.webid.controller;

import com.tcon.webid.dto.ChatListItemDto;
import com.tcon.webid.dto.UnreadCountDto;
import com.tcon.webid.service.ChatNotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import jakarta.servlet.http.HttpServletRequest;

/**
 * REST controller for chat notifications and metadata
 * Provides endpoints for users and vendors to manage their chat notifications
 */
@Slf4j
@RestController
@RequestMapping("/api/chat-notifications")
public class ChatNotificationController {

    @Autowired
    private ChatNotificationService chatNotificationService;

    /**
     * Get chat list for a user/vendor
     * Shows all active chats with last message preview and unread count
     *
     * @param userId MongoDB ObjectId of the user/vendor
     * @return List of chat items
     */
    @GetMapping("/{userId}/chats")
    public ResponseEntity<List<ChatListItemDto>> getChatList(@PathVariable String userId, HttpServletRequest request) {
        try {
            // Log remote caller details to help identify polling clients
            String remote = request != null ? request.getRemoteAddr() : "unknown";
            String ua = request != null ? request.getHeader("User-Agent") : "unknown";
            log.info("Fetching chat list for user: {} from {} UA={}", userId, remote, ua);

            List<ChatListItemDto> chatList = chatNotificationService.getChatList(userId);
            return ResponseEntity.ok(chatList);
        } catch (Exception e) {
            log.error("Error fetching chat list for user {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get total unread message count for a user/vendor
     * Useful for displaying notification badge
     *
     * @param userId MongoDB ObjectId of the user/vendor
     * @return UnreadCountDto with total count
     */
    @GetMapping("/{userId}/unread-count")
    public ResponseEntity<UnreadCountDto> getUnreadCount(@PathVariable String userId) {
        try {
            log.info("Fetching unread count for user: {}", userId);
            UnreadCountDto unreadCount = chatNotificationService.getUnreadCount(userId);
            return ResponseEntity.ok(unreadCount);
        } catch (Exception e) {
            log.error("Error fetching unread count for user {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Mark a chat as read (reset unread count to 0)
     * Call this when user opens a specific chat
     *
     * @param userId             MongoDB ObjectId of the user
     * @param otherParticipantId MongoDB ObjectId of the other participant
     * @return Success status
     */
    @PutMapping("/{userId}/mark-read/{otherParticipantId}")
    public ResponseEntity<Void> markChatAsRead(
            @PathVariable String userId,
            @PathVariable String otherParticipantId) {
        try {
            log.info("Marking chat as read for user {} with participant {}", userId, otherParticipantId);
            chatNotificationService.markChatAsRead(userId, otherParticipantId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error marking chat as read: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Delete a chat notification (remove from chat list)
     * Note: This doesn't delete actual messages, only the notification metadata
     *
     * @param userId             MongoDB ObjectId of the user
     * @param otherParticipantId MongoDB ObjectId of the other participant
     * @return Success status
     */
    @DeleteMapping("/{userId}/chats/{otherParticipantId}")
    public ResponseEntity<Void> deleteChat(
            @PathVariable String userId,
            @PathVariable String otherParticipantId) {
        try {
            log.info("Deleting chat for user {} with participant {}", userId, otherParticipantId);
            chatNotificationService.deleteChat(userId, otherParticipantId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error deleting chat: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Update online status for a user/vendor
     * This updates the online status shown in other users' chat lists
     *
     * @param userId MongoDB ObjectId of the user
     * @param status "ONLINE" or "OFFLINE"
     * @return Success status
     */
    @PutMapping("/{userId}/status")
    public ResponseEntity<Void> updateOnlineStatus(
            @PathVariable String userId,
            @RequestParam String status) {
        try {
            log.info("Updating online status for user {} to {}", userId, status);
            chatNotificationService.updateOnlineStatus(userId, status);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error updating online status: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Refresh participant info
     * Call this when a user/vendor updates their profile
     *
     * @param participantId MongoDB ObjectId of the user/vendor
     * @return Success status
     */
    @PutMapping("/{participantId}/refresh")
    public ResponseEntity<Void> refreshParticipantInfo(@PathVariable String participantId) {
        try {
            log.info("Refreshing participant info for: {}", participantId);
            chatNotificationService.refreshParticipantInfo(participantId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error refreshing participant info: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
