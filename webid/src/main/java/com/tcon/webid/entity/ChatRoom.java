package com.tcon.webid.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * ChatRoom entity representing a conversation between two participants.
 * Stores metadata about the chat including:
 * - Participant information
 * - Last message preview
 * - Unread counts for each participant
 * - Online/Typing status
 * - Last seen timestamps
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "chat_rooms")
@CompoundIndexes({
    @CompoundIndex(name = "participant1_v2_idx", def = "{'participant1Id': 1, 'lastMessageTimestamp': -1, 'active': 1}"),
    @CompoundIndex(name = "participant2_v2_idx", def = "{'participant2Id': 1, 'lastMessageTimestamp': -1, 'active': 1}"),
    @CompoundIndex(name = "chat_id_v2_unique_idx", def = "{'chatId': 1}", unique = true)
})
public class ChatRoom {

    @Id
    private String id;

    /**
     * Unique chat identifier (combination of participant IDs, alphabetically sorted)
     */
    @Indexed(unique = true)
    private String chatId;

    // ========== Participant 1 Info ==========

    /**
     * MongoDB ObjectId of participant 1
     */
    @Indexed
    private String participant1Id;

    /**
     * Type of participant 1: "USER" or "VENDOR"
     */
    private String participant1Type;

    /**
     * Display name of participant 1
     */
    private String participant1Name;

    /**
     * Profile image URL of participant 1
     */
    private String participant1ProfileUrl;

    /**
     * Unread message count for participant 1
     */
    @Builder.Default
    private int participant1UnreadCount = 0;

    /**
     * Online status of participant 1
     */
    @Builder.Default
    private boolean participant1Online = false;

    /**
     * Whether participant 1 is currently typing
     */
    @Builder.Default
    private boolean participant1Typing = false;

    /**
     * Last seen timestamp for participant 1 (ISO 8601)
     */
    private String participant1LastSeen;

    // ========== Participant 2 Info ==========

    /**
     * MongoDB ObjectId of participant 2
     */
    @Indexed
    private String participant2Id;

    /**
     * Type of participant 2: "USER" or "VENDOR"
     */
    private String participant2Type;

    /**
     * Display name of participant 2
     */
    private String participant2Name;

    /**
     * Profile image URL of participant 2
     */
    private String participant2ProfileUrl;

    /**
     * Unread message count for participant 2
     */
    @Builder.Default
    private int participant2UnreadCount = 0;

    /**
     * Online status of participant 2
     */
    @Builder.Default
    private boolean participant2Online = false;

    /**
     * Whether participant 2 is currently typing
     */
    @Builder.Default
    private boolean participant2Typing = false;

    /**
     * Last seen timestamp for participant 2 (ISO 8601)
     */
    private String participant2LastSeen;

    // ========== Last Message Info ==========

    /**
     * Content of the last message (truncated for preview)
     */
    private String lastMessageContent;

    /**
     * ID of the last message sender
     */
    private String lastMessageSenderId;

    /**
     * Timestamp of the last message (ISO 8601)
     */
    private String lastMessageTimestamp;

    /**
     * Status of the last message
     */
    private String lastMessageStatus;

    // ========== Metadata ==========

    /**
     * When this chat room was created (ISO 8601)
     */
    private String createdAt;

    /**
     * When this chat room was last updated (ISO 8601)
     */
    private String updatedAt;

    /**
     * Whether this chat room is active
     */
    @Builder.Default
    private boolean active = true;

    /**
     * Helper method to get participant info based on viewer's perspective
     */
    public ParticipantInfo getOtherParticipantInfo(String viewerId) {
        if (participant1Id != null && participant1Id.equals(viewerId)) {
            return ParticipantInfo.builder()
                    .participantId(participant2Id)
                    .participantType(participant2Type)
                    .participantName(participant2Name)
                    .participantProfileUrl(participant2ProfileUrl)
                    .online(participant2Online)
                    .typing(participant2Typing)
                    .lastSeen(participant2LastSeen)
                    .build();
        } else {
            return ParticipantInfo.builder()
                    .participantId(participant1Id)
                    .participantType(participant1Type)
                    .participantName(participant1Name)
                    .participantProfileUrl(participant1ProfileUrl)
                    .online(participant1Online)
                    .typing(participant1Typing)
                    .lastSeen(participant1LastSeen)
                    .build();
        }
    }

    /**
     * Get unread count for a specific participant
     */
    public int getUnreadCountFor(String participantId) {
        if (participant1Id != null && participant1Id.equals(participantId)) {
            return participant1UnreadCount;
        } else {
            return participant2UnreadCount;
        }
    }

    /**
     * Increment unread count for a specific participant
     */
    public void incrementUnreadCountFor(String participantId) {
        if (participant1Id != null && participant1Id.equals(participantId)) {
            participant1UnreadCount++;
        } else {
            participant2UnreadCount++;
        }
    }

    /**
     * Reset unread count for a specific participant
     */
    public void resetUnreadCountFor(String participantId) {
        if (participant1Id != null && participant1Id.equals(participantId)) {
            participant1UnreadCount = 0;
        } else {
            participant2UnreadCount = 0;
        }
    }

    /**
     * Update online status for a specific participant
     */
    public void setOnlineStatus(String participantId, boolean online) {
        String now = Instant.now().toString();
        if (participant1Id != null && participant1Id.equals(participantId)) {
            participant1Online = online;
            if (!online) {
                participant1LastSeen = now;
            }
        } else if (participant2Id != null && participant2Id.equals(participantId)) {
            participant2Online = online;
            if (!online) {
                participant2LastSeen = now;
            }
        }
        this.updatedAt = now;
    }

    /**
     * Update typing status for a specific participant
     */
    public void setTypingStatus(String participantId, boolean typing) {
        if (participant1Id != null && participant1Id.equals(participantId)) {
            participant1Typing = typing;
        } else if (participant2Id != null && participant2Id.equals(participantId)) {
            participant2Typing = typing;
        }
        this.updatedAt = Instant.now().toString();
    }

    /**
     * Check if a participant is online
     */
    public boolean isParticipantOnline(String participantId) {
        if (participant1Id != null && participant1Id.equals(participantId)) {
            return participant1Online;
        } else {
            return participant2Online;
        }
    }

    /**
     * Check if a participant is typing
     */
    public boolean isParticipantTyping(String participantId) {
        if (participant1Id != null && participant1Id.equals(participantId)) {
            return participant1Typing;
        } else {
            return participant2Typing;
        }
    }

    /**
     * Inner class for participant info
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParticipantInfo {
        private String participantId;
        private String participantType;
        private String participantName;
        private String participantProfileUrl;
        private boolean online;
        private boolean typing;
        private String lastSeen;
    }
}

