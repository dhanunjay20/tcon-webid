package com.tcon.webid.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "chat_notifications")
@CompoundIndexes({
    @CompoundIndex(name = "user_chat_idx", def = "{'userId': 1, 'lastMessageTimestamp': -1}"),
    @CompoundIndex(name = "unread_idx", def = "{'userId': 1, 'unreadCount': -1}")
})
public class ChatNotificationMetadata {

    @Id
    private String id;
    private String userId;
    private String otherParticipantId;
    private String otherParticipantName;
    private String otherParticipantType;
    private String otherParticipantProfileUrl;
    private String chatId;
    private String lastMessageContent;
    private String lastMessageSenderId;
    private String lastMessageTimestamp;
    private int unreadCount;
    private String onlineStatus;
    private boolean isTyping;
    private String createdAt;
    private String updatedAt;
}

