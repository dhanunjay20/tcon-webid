package com.tcon.webid.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for unread message count summary
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnreadCountDto {

    /**
     * MongoDB ObjectId of the user/vendor
     */
    private String userId;

    /**
     * Total number of unread messages across all chats
     */
    private int totalUnreadCount;

    /**
     * Number of chats with unread messages
     */
    private int unreadChatsCount;
}

