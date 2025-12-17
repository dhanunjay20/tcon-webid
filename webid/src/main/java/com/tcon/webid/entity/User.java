package com.tcon.webid.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class User {
    @Id
    private String id;
    private String firstName;
    private String lastName;
    @Indexed(unique = true)
    private String email;
    @Indexed(unique = true)
    private String mobile;
    private String passwordHash;
    private List<Address> addresses;
    private String profileUrl; // URL for user profile image/page

    // Live location fields (latitude/longitude) and last update timestamp
    private Double latitude;
    private Double longitude;
    private String lastLocationUpdatedAt;

    // Stripe customer ID for payment processing
    private String stripeCustomerId;
}
