package com.tcon.webid.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@Document(collection = "bids")
public class Bid {
    @Id
    private String id;
    private String orderId;
    private String vendorOrganizationId;
    private String proposedMessage;
    private double proposedTotalPrice;
    private String status; // requested, quoted, accepted, rejected
    private String submittedAt;
    private String updatedAt;
}
