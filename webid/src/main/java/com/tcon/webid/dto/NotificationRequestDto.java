package com.tcon.webid.dto;
import lombok.Data;

@Data
public class NotificationRequestDto {
    private String recipientUserId;
    private String recipientVendorOrgId; // Optional
    private String type;
    private String message;
    private String dataId;
    private String dataType;
}
