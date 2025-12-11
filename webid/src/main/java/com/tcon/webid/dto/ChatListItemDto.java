package com.tcon.webid.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for chat list item (used in chat list/inbox view).
 * Shows conversation preview with unread count and participant status.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChatListItemDto {

    /**
     * Chat room ID
     */
    private String chatId;

    /**
     * Other participant information
     */
    private String participantId;
    private String participantType;
    private String participantName;
    private String participantProfileUrl;

    /**
     * Last message preview
     */
    private String lastMessage;
    private String lastMessageSenderId;
    private String lastMessageTimestamp;
    private String lastMessageStatus;

    /**
     * Number of unread messages
     */
    private int unreadCount;

    /**
     * Participant status
     */
    private boolean online;
    private boolean typing;
    private String lastSeen;

    /**
     * Helper to format last seen for display
     */
    public String getLastSeenFormatted() {
        if (online) {
            return "Online";
        }
        if (typing) {
            return "Typing...";
        }
        if (lastSeen == null || lastSeen.isEmpty()) {
            return "Offline";
        }
        return "Last seen: " + lastSeen;
    }
}

