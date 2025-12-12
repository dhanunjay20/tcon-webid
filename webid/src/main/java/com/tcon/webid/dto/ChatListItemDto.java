package com.tcon.webid.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for chat list item with notification info
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatListItemDto {

    /**
     * MongoDB ObjectId of the other participant
     */
    private String participantId;

    /**
     * Name of the other participant
     */
    private String participantName;

    /**
     * Type: "USER" or "VENDOR"
     */
    private String participantType;

    /**
     * Profile URL of the other participant
     */
    private String participantProfileUrl;

    /**
     * Chat ID
     */
    private String chatId;

    /**
     * Last message preview
     */
    private String lastMessage;

    /**
     * Who sent the last message (MongoDB ObjectId)
     */
    private String lastMessageSenderId;

    /**
     * Timestamp of last message
     */
    private String lastMessageTimestamp;

    /**
     * Number of unread messages
     */
    private int unreadCount;

    /**
     * Online status: "ONLINE" or "OFFLINE"
     */
    private String onlineStatus;

    /**
     * Whether the participant is typing
     */
    private boolean isTyping;
}

