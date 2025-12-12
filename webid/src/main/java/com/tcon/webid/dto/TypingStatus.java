package com.tcon.webid.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonSetter;
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
     * Sender type: "VENDOR" or "USER"
     */
    private String senderType;

    /**
     * Whether the user is currently typing.
     * Accept either JSON property name `isTyping` or legacy `typing`.
     */
    @JsonProperty("isTyping")
    @JsonAlias({"typing"})
    private Boolean typing;

    /**
     * Explicit getter that preserves the previous method name used throughout the codebase.
     */
    public Boolean isTyping() {
        return this.typing;
    }

    // Single tolerant JsonSetter to avoid conflicting overloaded setters.
    @JsonSetter("isTyping")
    @JsonAlias({"typing"})
    public void setIsTyping(Object value) {
        if (value == null) {
            this.typing = null;
            return;
        }
        if (value instanceof Boolean) {
            this.typing = (Boolean) value;
            return;
        }
        if (value instanceof Number) {
            this.typing = ((Number) value).intValue() != 0;
            return;
        }
        // Fallback to string parsing
        String s = value.toString().trim().toLowerCase();
        if (s.equals("1") || s.equals("true") || s.equals("yes") || s.equals("y")) {
            this.typing = Boolean.TRUE;
        } else if (s.equals("0") || s.equals("false") || s.equals("no") || s.equals("n")) {
            this.typing = Boolean.FALSE;
        } else {
            // Last resort: Boolean.parseBoolean (false for unknown values)
            this.typing = Boolean.parseBoolean(s);
        }
    }
}