package com.tcon.webid.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * ChatMessage entity for storing chat messages in MongoDB
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "chat_messages")
public class ChatMessage {

    @Id
    private String id;

    /**
     * Unique chat identifier (combination of sender and recipient IDs)
     */
    private String chatId;

    /**
     * MongoDB ObjectId of the message sender (User or Vendor)
     * This should be the actual MongoDB _id from users or vendors collection
     */
    private String senderId;

    /**
     * MongoDB ObjectId of the message recipient (User or Vendor)
     * This should be the actual MongoDB _id from users or vendors collection
     */
    private String recipientId;

    /**
     * Message content
     */
    private String content;

    /**
     * Timestamp in ISO 8601 format
     */
    private String timestamp;

    /**
     * Message delivery status
     */
    private MessageStatus status;

    /**
     * Enum for message delivery status
     */
    public enum MessageStatus {
        SENT,       // Message sent by sender
        DELIVERED,  // Message delivered to recipient
        READ        // Message read by recipient
    }
}

