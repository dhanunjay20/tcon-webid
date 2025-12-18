package com.tcon.webid.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for popular menu items data
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PopularMenuItemDto {
    private String item;
    private Integer orders;
    private Double revenue;
}

