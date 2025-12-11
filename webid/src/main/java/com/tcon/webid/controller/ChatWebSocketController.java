package com.tcon.webid.controller;

import com.tcon.webid.dto.*;
import com.tcon.webid.service.ChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.security.Principal;

/**
 * WebSocket controller for real-time chat operations.
 * Handles all chat-related WebSocket messages including:
 * - Sending messages
 * - Typing indicators
 * - Read receipts
 * - Online status
 */
@Slf4j
@Controller
public class ChatWebSocketController {

    @Autowired
    private ChatService chatService;

    /**
     * Handle new chat message
     * Client sends to: /app/chat.send
     */
    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatMessageRequestDto messageRequest,
                           SimpMessageHeaderAccessor headerAccessor) {
        try {
            // Get sender info from session
            String senderId = getSenderId(headerAccessor);
            String senderType = getSenderType(headerAccessor);

            if (senderId == null) {
                log.warn("Cannot send message: sender not authenticated");
                return;
            }

            log.info("WebSocket message from {} ({}) to {}",
                    senderId, senderType, messageRequest.getRecipientId());

            // Process and send message
            chatService.sendMessage(senderId, senderType, messageRequest);

        } catch (Exception e) {
            log.error("Error processing WebSocket message: {}", e.getMessage(), e);
        }
    }

    /**
     * Handle typing indicator
     * Client sends to: /app/chat.typing
     */
    @MessageMapping("/chat.typing")
    public void handleTyping(@Payload TypingStatusDto typingStatus,
                            SimpMessageHeaderAccessor headerAccessor) {
        try {
            String senderId = getSenderId(headerAccessor);
            String senderType = getSenderType(headerAccessor);

            if (senderId == null) {
                log.warn("Cannot process typing: sender not authenticated");
                return;
            }

            // Override senderId from payload with authenticated user
            boolean isTyping = Boolean.TRUE.equals(typingStatus.isTyping());

            log.debug("Typing status from {}: {} to {}",
                    senderId, isTyping, typingStatus.getRecipientId());

            chatService.updateTypingStatus(senderId, senderType,
                    typingStatus.getRecipientId(), isTyping);

        } catch (Exception e) {
            log.error("Error processing typing status: {}", e.getMessage(), e);
        }
    }

    /**
     * Handle read receipt
     * Client sends to: /app/chat.read
     */
    @MessageMapping("/chat.read")
    public void handleReadReceipt(@Payload MessageReadReceiptDto readReceipt,
                                  SimpMessageHeaderAccessor headerAccessor) {
        try {
            String readerId = getSenderId(headerAccessor);

            if (readerId == null) {
                log.warn("Cannot process read receipt: user not authenticated");
                return;
            }

            log.info("Read receipt from {} for chat {}", readerId, readReceipt.getChatId());

            // Mark messages as read
            chatService.markMessagesAsRead(readReceipt.getChatId(), readerId);

        } catch (Exception e) {
            log.error("Error processing read receipt: {}", e.getMessage(), e);
        }
    }

    /**
     * Handle user going online
     * Client sends to: /app/chat.online
     *
     * NOTE: WebSocketEventListener already handles online status automatically on connection.
     * This is only needed if client wants to explicitly set status (e.g., from AWAY to ONLINE)
     */
    @MessageMapping("/chat.online")
    public void handleOnline(@Payload UserPresenceDto presenceDto,
                            SimpMessageHeaderAccessor headerAccessor) {
        try {
            String userId = getSenderId(headerAccessor);
            String userType = getSenderType(headerAccessor);

            if (userId == null) {
                log.warn("Cannot process online status: user not authenticated");
                return;
            }

            log.debug("User {} ({}) explicitly requested ONLINE status", userId, userType);

            // Only mark pending messages as delivered
            // (Status is already updated by WebSocketEventListener on connection)
            chatService.markMessagesAsDelivered(userId);

        } catch (Exception e) {
            log.error("Error processing online status: {}", e.getMessage(), e);
        }
    }

    /**
     * Handle user going offline
     * Client sends to: /app/chat.offline
     *
     * NOTE: WebSocketEventListener already handles offline status automatically on disconnection.
     * This is only needed if client wants to explicitly set status (e.g., from ONLINE to AWAY)
     * without disconnecting.
     */
    @MessageMapping("/chat.offline")
    public void handleOffline(@Payload UserPresenceDto presenceDto,
                             SimpMessageHeaderAccessor headerAccessor) {
        try {
            String userId = getSenderId(headerAccessor);
            String userType = getSenderType(headerAccessor);

            if (userId == null) {
                log.warn("Cannot process offline status: user not authenticated");
                return;
            }

            log.debug("User {} ({}) explicitly requested OFFLINE status (but staying connected)", userId, userType);

            // Update to AWAY status instead of OFFLINE (since they're still connected)
            // True OFFLINE is set by WebSocketEventListener on disconnect
            chatService.updateOnlineStatus(userId, userType, false);

        } catch (Exception e) {
            log.error("Error processing offline status: {}", e.getMessage(), e);
        }
    }

    /**
     * Handle chat opened event (for automatic read receipts)
     * Client sends to: /app/chat.open
     */
    @MessageMapping("/chat.open")
    public void handleChatOpened(@Payload MessageReadReceiptDto chatInfo,
                                 SimpMessageHeaderAccessor headerAccessor) {
        try {
            String userId = getSenderId(headerAccessor);

            if (userId == null) {
                log.warn("Cannot process chat open: user not authenticated");
                return;
            }

            log.info("User {} opened chat {}", userId, chatInfo.getChatId());

            // Mark all messages as read when chat is opened
            chatService.markMessagesAsRead(chatInfo.getChatId(), userId);

        } catch (Exception e) {
            log.error("Error processing chat opened: {}", e.getMessage(), e);
        }
    }

    /**
     * Get sender ID from WebSocket session
     */
    private String getSenderId(SimpMessageHeaderAccessor headerAccessor) {
        // Try to get from principal
        Principal principal = headerAccessor.getUser();
        if (principal != null) {
            return principal.getName();
        }

        // Try to get from session attributes
        Object userId = headerAccessor.getSessionAttributes().get("userId");
        if (userId != null) {
            return userId.toString();
        }

        return null;
    }

    /**
     * Get sender type from WebSocket session
     */
    private String getSenderType(SimpMessageHeaderAccessor headerAccessor) {
        // Try to get from session attributes
        Object userType = headerAccessor.getSessionAttributes().get("userType");
        if (userType != null) {
            return userType.toString();
        }

        // Default to USER if not specified
        return "USER";
    }
}

