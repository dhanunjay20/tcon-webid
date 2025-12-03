package com.tcon.webid.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for typing indicator status
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TypingStatus {

    /**
     * ID of the user who is typing
     */
    private String senderId;

    /**
     * ID of the recipient
     */
    private String recipientId;

    /**
     * Whether the user is currently typing
     */
    private boolean isTyping;
}
