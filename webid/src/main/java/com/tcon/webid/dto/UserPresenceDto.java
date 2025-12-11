package com.tcon.webid.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for user online/offline status.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPresenceDto {

    /**
     * User ID
     */
    private String userId;

    /**
     * User type: "USER" or "VENDOR"
     */
    private String userType;

    /**
     * Display name
     */
    private String displayName;

    /**
     * Online status: ONLINE, OFFLINE, AWAY, BUSY
     */
    private String status;

    /**
     * Last seen timestamp (ISO 8601)
     */
    private String lastSeen;

    /**
     * Last activity timestamp (ISO 8601)
     */
    private String lastActivity;

    /**
     * Custom status message
     */
    private String statusMessage;

    /**
     * Whether user is currently online
     */
    public boolean isOnline() {
        return "ONLINE".equals(status);
    }
}

