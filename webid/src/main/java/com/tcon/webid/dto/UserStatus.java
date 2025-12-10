package com.tcon.webid.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for user online/offline status
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatus {

    /**
     * User ID
     */
    private String userId;

    /**
     * User status (ONLINE/OFFLINE)
     */
    private String status;
}
