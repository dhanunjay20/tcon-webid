package com.tcon.webid.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for chat notification sent to recipient
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatNotification {

    /**
     * Message ID
     */
    private String id;

    /**
     * ID of the message sender
     */
    private String senderId;

    /**
     * Message content
     */
    private String content;
}

