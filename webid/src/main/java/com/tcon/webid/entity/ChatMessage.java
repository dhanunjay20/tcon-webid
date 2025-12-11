package com.tcon.webid.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * ChatMessage entity for storing chat messages in MongoDB.
 * Supports real-time messaging with WhatsApp-like features:
 * - Message status tracking (SENDING, SENT, DELIVERED, READ, FAILED)
 * - Timestamps for each status change
 * - Support for both User-to-Vendor and Vendor-to-User messaging
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "chat_messages")
@CompoundIndexes({
    @CompoundIndex(name = "chat_v2_idx", def = "{'chatId': 1, 'timestamp': 1, 'deleted': 1}"),
    @CompoundIndex(name = "sender_recipient_v2_idx", def = "{'senderId': 1, 'recipientId': 1, 'status': 1, 'deleted': 1}"),
    @CompoundIndex(name = "recipient_status_v2_idx", def = "{'recipientId': 1, 'status': 1, 'deleted': 1}")
})
public class ChatMessage {

    @Id
    private String id;

    /**
     * Unique chat identifier (combination of sender and recipient IDs, alphabetically sorted)
     */
    @Indexed
    private String chatId;

    /**
     * MongoDB ObjectId of the message sender (User or Vendor)
     */
    @Indexed
    private String senderId;

    /**
     * Type of sender: "USER" or "VENDOR"
     */
    private String senderType;

    /**
     * Name of the sender (for display purposes)
     */
    private String senderName;

    /**
     * MongoDB ObjectId of the message recipient (User or Vendor)
     */
    @Indexed
    private String recipientId;

    /**
     * Type of recipient: "USER" or "VENDOR"
     */
    private String recipientType;

    /**
     * Name of the recipient (for display purposes)
     */
    private String recipientName;

    /**
     * Message content
     */
    private String content;

    /**
     * Message type: TEXT, IMAGE, FILE, SYSTEM
     */
    @Builder.Default
    private MessageType messageType = MessageType.TEXT;

    /**
     * URL for media messages (images, files)
     */
    private String mediaUrl;

    /**
     * File name for file attachments
     */
    private String fileName;

    /**
     * Message delivery status
     */
    @Builder.Default
    private MessageStatus status = MessageStatus.SENDING;

    /**
     * Timestamp when message was created (ISO 8601 format)
     */
    private String timestamp;

    /**
     * Timestamp when message was sent to server
     */
    private String sentAt;

    /**
     * Timestamp when message was delivered to recipient's device
     */
    private String deliveredAt;

    /**
     * Timestamp when message was read by recipient
     */
    private String readAt;

    /**
     * Error message if delivery failed
     */
    private String errorMessage;

    /**
     * Client-generated temporary ID for optimistic UI updates
     */
    private String tempId;

    /**
     * Whether this message has been deleted
     */
    @Builder.Default
    private boolean deleted = false;

    /**
     * Timestamp when message was deleted
     */
    private String deletedAt;

    /**
     * Enum for message delivery status (WhatsApp-like)
     */
    public enum MessageStatus {
        SENDING,    // Message is being sent (client-side only, used for optimistic UI)
        SENT,       // Message sent to server (single tick)
        DELIVERED,  // Message delivered to recipient's device (double tick)
        READ,       // Message read by recipient (blue double tick)
        FAILED      // Message delivery failed
    }

    /**
     * Enum for message types
     */
    public enum MessageType {
        TEXT,       // Regular text message
        IMAGE,      // Image attachment
        FILE,       // File attachment
        SYSTEM      // System message (e.g., "User joined chat")
    }

    /**
     * Helper method to generate chat ID from two user IDs
     * Ensures consistent chat ID regardless of who sends first
     */
    public static String generateChatId(String id1, String id2) {
        if (id1 == null || id2 == null) {
            throw new IllegalArgumentException("Both IDs must be non-null");
        }
        return id1.compareTo(id2) < 0 ? id1 + "_" + id2 : id2 + "_" + id1;
    }

    /**
     * Set timestamp to current time if not set
     */
    public void initializeTimestamps() {
        String now = Instant.now().toString();
        if (this.timestamp == null) {
            this.timestamp = now;
        }
    }
}

