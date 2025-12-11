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
 * UserPresence entity for tracking real-time online/offline status.
 * This is separate from ChatRoom to handle multi-device and global presence tracking.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "user_presence")
@CompoundIndexes({
    @CompoundIndex(name = "user_type_idx", def = "{'userId': 1, 'userType': 1}", unique = true)
})
public class UserPresence {

    @Id
    private String id;

    /**
     * MongoDB ObjectId of the user/vendor
     */
    @Indexed(unique = true)
    private String userId;

    /**
     * Type: "USER" or "VENDOR"
     */
    private String userType;

    /**
     * Display name
     */
    private String displayName;

    /**
     * Profile URL
     */
    private String profileUrl;

    /**
     * Current online status
     */
    @Builder.Default
    private PresenceStatus status = PresenceStatus.OFFLINE;

    /**
     * Last seen timestamp (ISO 8601)
     */
    private String lastSeen;

    /**
     * Last activity timestamp (ISO 8601) - updated on any action
     */
    private String lastActivity;

    /**
     * Number of active WebSocket connections (for multi-device support)
     */
    @Builder.Default
    private int activeConnections = 0;

    /**
     * Custom status message (optional)
     */
    private String statusMessage;

    /**
     * Created timestamp
     */
    private String createdAt;

    /**
     * Updated timestamp
     */
    private String updatedAt;

    /**
     * Enum for presence status
     */
    public enum PresenceStatus {
        ONLINE,     // User is currently online
        AWAY,       // User is idle/away
        BUSY,       // User is busy (Do Not Disturb)
        OFFLINE     // User is offline
    }

    /**
     * Update presence to online
     */
    public void goOnline() {
        this.status = PresenceStatus.ONLINE;
        this.lastActivity = Instant.now().toString();
        this.activeConnections++;
        this.updatedAt = Instant.now().toString();
    }

    /**
     * Update presence to offline
     */
    public void goOffline() {
        this.activeConnections = Math.max(0, this.activeConnections - 1);
        if (this.activeConnections == 0) {
            this.status = PresenceStatus.OFFLINE;
            this.lastSeen = Instant.now().toString();
        }
        this.updatedAt = Instant.now().toString();
    }

    /**
     * Force offline (disconnect all sessions)
     */
    public void forceOffline() {
        this.status = PresenceStatus.OFFLINE;
        this.activeConnections = 0;
        this.lastSeen = Instant.now().toString();
        this.updatedAt = Instant.now().toString();
    }

    /**
     * Check if truly online (has active connections)
     */
    public boolean isTrulyOnline() {
        return this.status == PresenceStatus.ONLINE && this.activeConnections > 0;
    }
}

