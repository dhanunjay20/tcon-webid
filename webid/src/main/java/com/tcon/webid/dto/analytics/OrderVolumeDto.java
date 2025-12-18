package com.tcon.webid.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for order volume data over time
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderVolumeDto {
    private String date;
    private Integer orders;
    private Integer completed;
}

