package com.tcon.webid.service;

import com.tcon.webid.dto.UserRegistrationDto;
import com.tcon.webid.dto.UserUpdateDto;
import com.tcon.webid.entity.User;

import java.util.List;

public interface UserService {
    User createUser(UserRegistrationDto dto);
    User getUserById(String id);
    List<User> getAllUsers();
    User updateUser(String id, UserUpdateDto dto);
    void deleteUser(String id);
}
