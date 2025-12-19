package com.tcon.webid.service;

import com.tcon.webid.dto.admin.*;
import java.util.List;

public interface AdminService {

    // Admin Authentication
    AdminResponseDto registerAdmin(AdminRegistrationDto dto);
    AdminResponseDto loginAdmin(String username, String password);
    AdminResponseDto getAdminById(String id);
    List<AdminResponseDto> getAllAdmins();

    // Dashboard Stats
    AdminDashboardStatsDto getDashboardStats();

    // User Management
    List<AdminUserDto> getAllUsers();
    AdminUserDto getUserById(String id);
    void deleteUser(String id);

    // Vendor Management
    List<AdminVendorDto> getAllVendors();
    AdminVendorDto getVendorById(String id);
    void deleteVendor(String id);
    AdminVendorDto updateVendorStatus(String id, Boolean isActive);

    // Order Management
    List<AdminOrderDto> getAllOrders();
    AdminOrderDto getOrderById(String id);
    void deleteOrder(String id);
    AdminOrderDto updateOrderStatus(String id, String status);

    // Bid Management
    List<AdminBidDto> getAllBids();
    AdminBidDto getBidById(String id);
    void deleteBid(String id);

    // Menu Item Management
    List<AdminMenuItemDto> getAllMenuItems();
    AdminMenuItemDto getMenuItemById(String id);
    void deleteMenuItem(String id);

    // Payment Management
    List<AdminPaymentDto> getAllPayments();
    AdminPaymentDto getPaymentById(String id);
}

