package com.tcon.webid.controller;

import com.tcon.webid.dto.analytics.*;
import com.tcon.webid.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for analytics and dashboard data
 */
@Slf4j
@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    /**
     * Get monthly revenue comparison (current year vs last year)
     * GET /api/analytics/vendor/{vendorOrganizationId}/monthly-comparison
     */
    @GetMapping("/vendor/{vendorOrganizationId}/monthly-comparison")
    public ResponseEntity<List<MonthlyComparisonDto>> getMonthlyComparison(
            @PathVariable String vendorOrganizationId) {
        try {
            log.info("Fetching monthly comparison for vendorOrganizationId: {}", vendorOrganizationId);
            List<MonthlyComparisonDto> data = analyticsService.getMonthlyComparison(vendorOrganizationId);
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            log.error("Error fetching monthly comparison for vendorOrganizationId {}: {}", vendorOrganizationId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get order volume trends over time
     * GET /api/analytics/vendor/{vendorOrganizationId}/order-volume
     */
    @GetMapping("/vendor/{vendorOrganizationId}/order-volume")
    public ResponseEntity<List<OrderVolumeDto>> getOrderVolume(
            @PathVariable String vendorOrganizationId) {
        try {
            log.info("Fetching order volume for vendorOrganizationId: {}", vendorOrganizationId);
            List<OrderVolumeDto> data = analyticsService.getOrderVolumeTrends(vendorOrganizationId);
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            log.error("Error fetching order volume for vendorOrganizationId {}: {}", vendorOrganizationId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get popular menu items by order count
     * GET /api/analytics/vendor/{vendorOrganizationId}/popular-menu-items
     */
    @GetMapping("/vendor/{vendorOrganizationId}/popular-menu-items")
    public ResponseEntity<List<PopularMenuItemDto>> getPopularMenuItems(
            @PathVariable String vendorOrganizationId,
            @RequestParam(defaultValue = "6") int limit) {
        try {
            log.info("Fetching popular menu items for vendorOrganizationId: {}", vendorOrganizationId);
            List<PopularMenuItemDto> data = analyticsService.getPopularMenuItems(vendorOrganizationId, limit);
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            log.error("Error fetching popular menu items for vendorOrganizationId {}: {}", vendorOrganizationId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get revenue trends with targets
     * GET /api/analytics/vendor/{vendorOrganizationId}/revenue-trends
     */
    @GetMapping("/vendor/{vendorOrganizationId}/revenue-trends")
    public ResponseEntity<List<RevenueTrendDto>> getRevenueTrends(
            @PathVariable String vendorOrganizationId) {
        try {
            log.info("Fetching revenue trends for vendorOrganizationId: {}", vendorOrganizationId);
            List<RevenueTrendDto> data = analyticsService.getRevenueTrends(vendorOrganizationId);
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            log.error("Error fetching revenue trends for vendorOrganizationId {}: {}", vendorOrganizationId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get recent orders for orders table
     * GET /api/analytics/vendor/{vendorOrganizationId}/recent-orders
     */
    @GetMapping("/vendor/{vendorOrganizationId}/recent-orders")
    public ResponseEntity<List<OrderTableDto>> getRecentOrders(
            @PathVariable String vendorOrganizationId,
            @RequestParam(defaultValue = "10") int limit) {
        try {
            log.info("Fetching recent orders for vendorOrganizationId: {}", vendorOrganizationId);
            List<OrderTableDto> data = analyticsService.getRecentOrders(vendorOrganizationId, limit);
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            log.error("Error fetching recent orders for vendorOrganizationId {}: {}", vendorOrganizationId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get recent activity feed
     * GET /api/analytics/vendor/{vendorOrganizationId}/recent-activities
     */
    @GetMapping("/vendor/{vendorOrganizationId}/recent-activities")
    public ResponseEntity<List<RecentActivityDto>> getRecentActivities(
            @PathVariable String vendorOrganizationId,
            @RequestParam(defaultValue = "10") int limit) {
        try {
            log.info("Fetching recent activities for vendorOrganizationId: {}", vendorOrganizationId);
            List<RecentActivityDto> data = analyticsService.getRecentActivities(vendorOrganizationId, limit);
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            log.error("Error fetching recent activities for vendorOrganizationId {}: {}", vendorOrganizationId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get complete dashboard data in a single request
     * GET /api/analytics/vendor/{vendorOrganizationId}/dashboard
     */
    @GetMapping("/vendor/{vendorOrganizationId}/dashboard")
    public ResponseEntity<DashboardDataDto> getCompleteDashboard(
            @PathVariable String vendorOrganizationId) {
        try {
            log.info("Fetching complete dashboard data for vendorOrganizationId: {}", vendorOrganizationId);
            DashboardDataDto data = analyticsService.getCompleteDashboardData(vendorOrganizationId);
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            log.error("Error fetching complete dashboard for vendorOrganizationId {}: {}", vendorOrganizationId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}
