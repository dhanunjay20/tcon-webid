package com.tcon.webid.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for revenue trends data
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevenueTrendDto {
    private String month;
    private Double revenue;
    private Double target;
}

