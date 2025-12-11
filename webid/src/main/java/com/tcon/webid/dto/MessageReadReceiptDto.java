package com.tcon.webid.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for marking messages as read/delivered.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageReadReceiptDto {

    /**
     * Chat room ID
     */
    private String chatId;

    /**
     * ID of the user marking messages as read
     */
    private String readerId;

    /**
     * Type of reader: "USER" or "VENDOR"
     */
    private String readerType;

    /**
     * ID of the other participant (whose messages are being marked as read)
     */
    private String senderId;

    /**
     * Specific message ID to mark as read (optional - if null, mark all as read)
     */
    private String messageId;

    /**
     * Timestamp up to which messages should be marked as read (optional)
     */
    private String upToTimestamp;
}

