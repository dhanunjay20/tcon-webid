package com.tcon.webid.dto.admin;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminDashboardStatsDto {
    private long totalUsers;
    private long totalVendors;
    private long totalOrders;
    private long totalBids;
    private long totalMenuItems;
    private long totalPayments;
    private double totalRevenue;
    private long pendingOrders;
    private long completedOrders;
    private long activeVendors;
}

