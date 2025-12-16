package com.tcon.webid.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
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

    /**
     * Setter for typing status - handles Boolean inputs (preferred)
     */
    @JsonSetter("isTyping")
    public void setIsTyping(Boolean value) {
        this.typing = value;
    }

    /**
     * Alternative setter for "typing" property (for backward compatibility)
     */
    @JsonSetter("typing")
    public void setTyping(Boolean value) {
        this.typing = value;
    }

    /**
     * Generic setter that accepts String/Number/Boolean for backward compatibility and lenient parsing.
     * Jackson will use the best matching setter; this method is a fallback for unexpected types.
     */
    @JsonSetter("isTyping")
    public void setIsTyping(Object value) {
        this.typing = parseToBoolean(value);
    }

    @JsonSetter("typing")
    public void setTyping(Object value) {
        this.typing = parseToBoolean(value);
    }

    private Boolean parseToBoolean(Object value) {
        if (value == null) return Boolean.FALSE;
        if (value instanceof Boolean) return (Boolean) value;
        if (value instanceof Number) return ((Number) value).intValue() != 0;
        String s = value.toString().trim();
        if (s.isEmpty()) return Boolean.FALSE;
        // Accept "1","0","true","false","yes","no"
        if (s.equalsIgnoreCase("1") || s.equalsIgnoreCase("true") || s.equalsIgnoreCase("yes") || s.equalsIgnoreCase("y")) return Boolean.TRUE;
        return Boolean.FALSE;
    }
}