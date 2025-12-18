package com.tcon.webid.service;

import com.tcon.webid.dto.analytics.*;

import java.util.List;

/**
 * Service interface for dashboard analytics data
 */
public interface AnalyticsService {

    /**
     * Get monthly revenue comparison for current and previous year
     * @param vendorId Vendor MongoDB object id
     * @return List of monthly comparison data
     */
    List<MonthlyComparisonDto> getMonthlyComparison(String vendorId);

    /**
     * Get order volume trends over time (weekly)
     * @param vendorId Vendor MongoDB object id
     * @return List of order volume data
     */
    List<OrderVolumeDto> getOrderVolumeTrends(String vendorId);

    /**
     * Get popular menu items by order count
     * @param vendorId Vendor MongoDB object id
     * @param limit Number of items to return
     * @return List of popular menu items
     */
    List<PopularMenuItemDto> getPopularMenuItems(String vendorId, int limit);

    /**
     * Get revenue trends with targets
     * @param vendorId Vendor MongoDB object id
     * @return List of revenue trend data
     */
    List<RevenueTrendDto> getRevenueTrends(String vendorId);

    /**
     * Get recent orders for display in orders table
     * @param vendorId Vendor MongoDB object id
     * @param limit Number of orders to return
     * @return List of recent orders
     */
    List<OrderTableDto> getRecentOrders(String vendorId, int limit);

    /**
     * Get recent activity feed
     * @param vendorId Vendor MongoDB object id
     * @param limit Number of activities to return
     * @return List of recent activities
     */
    List<RecentActivityDto> getRecentActivities(String vendorId, int limit);

    /**
     * Get complete dashboard data in one call
     * @param vendorId Vendor MongoDB object id
     * @return Complete dashboard data
     */
    DashboardDataDto getCompleteDashboardData(String vendorId);
}
