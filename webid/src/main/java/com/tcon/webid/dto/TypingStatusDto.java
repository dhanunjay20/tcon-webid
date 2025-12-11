package com.tcon.webid.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for typing indicator status.
 * Sent when user starts or stops typing.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TypingStatusDto {

    /**
     * ID of the user who is typing
     */
    private String senderId;

    /**
     * Type of sender: "USER" or "VENDOR"
     */
    private String senderType;

    /**
     * ID of the recipient
     */
    private String recipientId;

    /**
     * Chat room ID
     */
    private String chatId;

    /**
     * Whether the user is currently typing
     */
    @JsonProperty("isTyping")
    private Boolean typing;

    /**
     * Explicit getter for backward compatibility
     */
    public Boolean isTyping() {
        return this.typing;
    }
}

