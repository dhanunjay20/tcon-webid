package com.tcon.webid.service;

import com.tcon.webid.dto.LoginRequestDto;
import com.tcon.webid.dto.AuthResponseDto;

public interface AuthService {
    AuthResponseDto login(LoginRequestDto loginDto);
    void sendUsernameToUser(String contact);
}

