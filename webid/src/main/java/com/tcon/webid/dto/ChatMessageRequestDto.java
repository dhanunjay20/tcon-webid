package com.tcon.webid.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for sending a new chat message.
 * Used by clients when sending messages via WebSocket or REST API.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChatMessageRequestDto {

    /**
     * MongoDB ObjectId of the recipient (User or Vendor)
     */
    private String recipientId;

    /**
     * Type of recipient: "USER" or "VENDOR"
     */
    private String recipientType;

    /**
     * Message content
     */
    private String content;

    /**
     * Message type: TEXT, IMAGE, FILE (defaults to TEXT)
     */
    private String messageType;

    /**
     * URL for media messages (images, files)
     */
    private String mediaUrl;

    /**
     * File name for file attachments
     */
    private String fileName;

    /**
     * Client-generated temporary ID for optimistic UI updates
     * Frontend should generate a unique ID for each message
     */
    private String tempId;
}

