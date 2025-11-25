package com.tcon.webid.dto;

import lombok.Data;

@Data
public class ForgotUsernameDto {
    // Email is optional - no validation to allow null
    private String email;

    // Mobile is optional - no validation to allow null
    private String mobile;
}
