package com.tcon.webid.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for order details in orders table
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderTableDto {
    private String id;
    private String client;
    private String event;
    private String date;
    private Integer guests;
    private String status;
    private String amount;
}

