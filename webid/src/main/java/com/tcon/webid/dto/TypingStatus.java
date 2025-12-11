package com.tcon.webid.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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
     * Vendor ID (sent by frontend)
     */
    private String vendorId;

    /**
     * Whether the user is currently typing.
     * Backing field is named `typing` to avoid Lombok/Jackson getter conflicts,
     * but the JSON property name remains `isTyping`.
     */
    @JsonProperty("isTyping")
    private Boolean typing;

    /**
     * Explicit getter that preserves the previous method name used throughout the codebase.
     */
    public Boolean isTyping() {
        return this.typing;
    }
}