package com.tcon.webid.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for monthly revenue comparison data
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyComparisonDto {
    private String month;
    private Double thisYear;
    private Double lastYear;
}

