package com.tcon.webid.dto.admin;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminResponseDto {
    private String id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String role;
    private String createdAt;
    private String lastLoginAt;
    private boolean isActive;
    private String token; // JWT token for authentication
}

