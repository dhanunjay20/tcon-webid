package com.tcon.webid.dto.admin;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import com.tcon.webid.entity.Address;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminUserDto {
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String mobile;
    private List<Address> addresses;
    private String profileUrl;
    private Double latitude;
    private Double longitude;
    private String lastLocationUpdatedAt;
    private String stripeCustomerId;
}

