package com.tcon.webid.dto;

import lombok.Data;
import com.tcon.webid.entity.Address;
import java.util.List;

@Data
public class UserResponseDto {
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String mobile;
    private List<Address> addresses;
    private String profileUrl;
}
