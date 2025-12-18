package com.tcon.webid.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Complete dashboard response with all analytics data
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardDataDto {
    private List<MonthlyComparisonDto> monthlyComparison;
    private List<OrderVolumeDto> orderVolume;
    private List<PopularMenuItemDto> popularMenuItems;
    private List<RevenueTrendDto> revenueTrends;
    private List<OrderTableDto> recentOrders;
    private List<RecentActivityDto> recentActivities;
}

