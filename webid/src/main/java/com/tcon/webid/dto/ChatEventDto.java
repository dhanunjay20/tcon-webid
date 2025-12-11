package com.tcon.webid.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * DTO for real-time WebSocket events.
 * This is the main payload sent through WebSocket for all chat-related events.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChatEventDto {

    /**
     * Type of event
     */
    private EventType eventType;

    /**
     * Chat room ID
     */
    private String chatId;

    /**
     * Message details (for message-related events)
     */
    private String messageId;
    private String tempId;
    private String content;
    private String messageType;
    private String messageStatus;
    private String mediaUrl;

    /**
     * Sender information
     */
    private String senderId;
    private String senderType;
    private String senderName;

    /**
     * Recipient information
     */
    private String recipientId;
    private String recipientType;
    private String recipientName;

    /**
     * Status information (for presence events)
     */
    private boolean online;
    private boolean typing;
    private String lastSeen;

    /**
     * Timestamps
     */
    private String timestamp;
    private String sentAt;
    private String deliveredAt;
    private String readAt;

    /**
     * Error information
     */
    private String errorMessage;
    private String errorCode;

    /**
     * Unread count (for count update events)
     */
    private int unreadCount;
    private int totalUnreadCount;

    /**
     * Event types for real-time WebSocket communication
     */
    public enum EventType {
        // Message events
        MESSAGE_NEW,           // New message received
        MESSAGE_SENT,          // Message sent confirmation (with server ID)
        MESSAGE_DELIVERED,     // Message delivered to recipient
        MESSAGE_READ,          // Message read by recipient
        MESSAGE_FAILED,        // Message delivery failed
        MESSAGE_DELETED,       // Message was deleted

        // Status events
        TYPING_START,          // User started typing
        TYPING_STOP,           // User stopped typing
        USER_ONLINE,           // User came online
        USER_OFFLINE,          // User went offline
        USER_AWAY,             // User is away

        // Chat events
        CHAT_OPENED,           // User opened a chat (for read receipts)
        CHAT_CLOSED,           // User closed a chat
        UNREAD_COUNT_UPDATE,   // Unread count changed

        // System events
        CONNECTION_ACK,        // Connection acknowledged
        ERROR,                 // Error occurred
        HEARTBEAT              // Keep-alive heartbeat
    }

    /**
     * Create a builder with timestamp initialized
     */
    public static ChatEventDtoBuilder builder() {
        return new ChatEventDtoBuilder().timestamp(Instant.now().toString());
    }

    /**
     * Factory methods for common events
     */
    public static ChatEventDto newMessage(String chatId, String messageId, String tempId,
                                          String senderId, String recipientId, String content) {
        return ChatEventDto.builder()
                .eventType(EventType.MESSAGE_NEW)
                .chatId(chatId)
                .messageId(messageId)
                .tempId(tempId)
                .senderId(senderId)
                .recipientId(recipientId)
                .content(content)
                .messageStatus("SENT")
                .build();
    }

    public static ChatEventDto messageDelivered(String chatId, String messageId, String senderId) {
        return ChatEventDto.builder()
                .eventType(EventType.MESSAGE_DELIVERED)
                .chatId(chatId)
                .messageId(messageId)
                .senderId(senderId)
                .messageStatus("DELIVERED")
                .deliveredAt(Instant.now().toString())
                .build();
    }

    public static ChatEventDto messageRead(String chatId, String messageId, String senderId) {
        return ChatEventDto.builder()
                .eventType(EventType.MESSAGE_READ)
                .chatId(chatId)
                .messageId(messageId)
                .senderId(senderId)
                .messageStatus("READ")
                .readAt(Instant.now().toString())
                .build();
    }

    public static ChatEventDto typingStatus(String chatId, String senderId, String recipientId, boolean typing) {
        return ChatEventDto.builder()
                .eventType(typing ? EventType.TYPING_START : EventType.TYPING_STOP)
                .chatId(chatId)
                .senderId(senderId)
                .recipientId(recipientId)
                .typing(typing)
                .build();
    }

    public static ChatEventDto onlineStatus(String userId, boolean online, String lastSeen) {
        return ChatEventDto.builder()
                .eventType(online ? EventType.USER_ONLINE : EventType.USER_OFFLINE)
                .senderId(userId)
                .online(online)
                .lastSeen(lastSeen)
                .build();
    }

    public static ChatEventDto error(String errorMessage, String errorCode) {
        return ChatEventDto.builder()
                .eventType(EventType.ERROR)
                .errorMessage(errorMessage)
                .errorCode(errorCode)
                .build();
    }

    public static ChatEventDto unreadCountUpdate(String recipientId, int unreadCount, int totalUnreadCount) {
        return ChatEventDto.builder()
                .eventType(EventType.UNREAD_COUNT_UPDATE)
                .recipientId(recipientId)
                .unreadCount(unreadCount)
                .totalUnreadCount(totalUnreadCount)
                .build();
    }
}

