package com.tcon.webid.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * DTO for real-time chat update notifications via WebSocket
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatUpdateNotification {
    private String messageId;
    private String chatId;
    private String senderId;
    private String recipientId;
    private String content;
    private String eventType; // MESSAGE_SENT, MESSAGE_DELIVERED, MESSAGE_READ, MESSAGE_DELETED, TYPING_START, TYPING_STOP
    private String messageStatus; // SENT, DELIVERED, READ
    private String timestamp;
    private String senderName;
    private String recipientName;
    private int unreadCount;

    // Add default timestamp on creation
    public static ChatUpdateNotificationBuilder builder() {
        return new ChatUpdateNotificationBuilder().timestamp(Instant.now().toString());
    }
}

