package com.tcon.webid.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebhookEventDto {

    private String eventId;

    private String eventType;

    private Map<String, Object> eventData;

    private String objectId;

    private String status;

    private Long timestamp;
}

