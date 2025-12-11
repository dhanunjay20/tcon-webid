package com.tcon.webid.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for chat message response.
 * Includes all message details and status information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChatMessageResponseDto {

    /**
     * MongoDB ObjectId of the message
     */
    private String id;

    /**
     * Chat room ID
     */
    private String chatId;

    /**
     * Sender information
     */
    private String senderId;
    private String senderType;
    private String senderName;

    /**
     * Recipient information
     */
    private String recipientId;
    private String recipientType;
    private String recipientName;

    /**
     * Message content
     */
    private String content;

    /**
     * Message type: TEXT, IMAGE, FILE, SYSTEM
     */
    private String messageType;

    /**
     * URL for media messages
     */
    private String mediaUrl;

    /**
     * File name for file attachments
     */
    private String fileName;

    /**
     * Message status: SENDING, SENT, DELIVERED, READ, FAILED
     */
    private String status;

    /**
     * Timestamps
     */
    private String timestamp;
    private String sentAt;
    private String deliveredAt;
    private String readAt;

    /**
     * Error message if delivery failed
     */
    private String errorMessage;

    /**
     * Client-generated temporary ID for correlation
     */
    private String tempId;

    /**
     * Whether the message was sent by the current user
     */
    private boolean isOwnMessage;
}

