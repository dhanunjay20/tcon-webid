package com.tcon.webid.controller;

import com.tcon.webid.dto.admin.*;
import com.tcon.webid.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Comprehensive Admin Controller for managing all system entities
 */
@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    // ==================== Admin Authentication ====================

    /**
     * Register a new admin
     * POST /api/admin/register
     */
    @PostMapping("/register")
    public ResponseEntity<AdminResponseDto> registerAdmin(@RequestBody AdminRegistrationDto dto) {
        try {
            log.info("Admin registration request: {}", dto.getUsername());
            AdminResponseDto response = adminService.registerAdmin(dto);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error registering admin: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Admin login
     * POST /api/admin/login
     */
    @PostMapping("/login")
    public ResponseEntity<AdminResponseDto> loginAdmin(@RequestBody AdminLoginDto dto) {
        try {
            log.info("Admin login request: {}", dto.getUsername());
            AdminResponseDto response = adminService.loginAdmin(dto.getUsername(), dto.getPassword());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error logging in admin: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get admin by ID
     * GET /api/admin/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<AdminResponseDto> getAdminById(@PathVariable String id) {
        try {
            AdminResponseDto response = adminService.getAdminById(id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching admin: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get all admins
     * GET /api/admin
     */
    @GetMapping
    public ResponseEntity<List<AdminResponseDto>> getAllAdmins() {
        try {
            List<AdminResponseDto> admins = adminService.getAllAdmins();
            return ResponseEntity.ok(admins);
        } catch (Exception e) {
            log.error("Error fetching admins: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // ==================== Dashboard Stats ====================

    /**
     * Get admin dashboard statistics
     * GET /api/admin/dashboard/stats
     */
    @GetMapping("/dashboard/stats")
    public ResponseEntity<AdminDashboardStatsDto> getDashboardStats() {
        try {
            log.info("Fetching admin dashboard stats");
            AdminDashboardStatsDto stats = adminService.getDashboardStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error fetching dashboard stats: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // ==================== User Management ====================

    /**
     * Get all users
     * GET /api/admin/users
     */
    @GetMapping("/users")
    public ResponseEntity<List<AdminUserDto>> getAllUsers() {
        try {
            log.info("Fetching all users");
            List<AdminUserDto> users = adminService.getAllUsers();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            log.error("Error fetching users: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get user by ID
     * GET /api/admin/users/{id}
     */
    @GetMapping("/users/{id}")
    public ResponseEntity<AdminUserDto> getUserById(@PathVariable String id) {
        try {
            AdminUserDto user = adminService.getUserById(id);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            log.error("Error fetching user: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Delete user
     * DELETE /api/admin/users/{id}
     */
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        try {
            log.info("Deleting user: {}", id);
            adminService.deleteUser(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error deleting user: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // ==================== Vendor Management ====================

    /**
     * Get all vendors
     * GET /api/admin/vendors
     */
    @GetMapping("/vendors")
    public ResponseEntity<List<AdminVendorDto>> getAllVendors() {
        try {
            log.info("Fetching all vendors");
            List<AdminVendorDto> vendors = adminService.getAllVendors();
            return ResponseEntity.ok(vendors);
        } catch (Exception e) {
            log.error("Error fetching vendors: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get vendor by ID
     * GET /api/admin/vendors/{id}
     */
    @GetMapping("/vendors/{id}")
    public ResponseEntity<AdminVendorDto> getVendorById(@PathVariable String id) {
        try {
            AdminVendorDto vendor = adminService.getVendorById(id);
            return ResponseEntity.ok(vendor);
        } catch (Exception e) {
            log.error("Error fetching vendor: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Update vendor status
     * PUT /api/admin/vendors/{id}/status
     */
    @PutMapping("/vendors/{id}/status")
    public ResponseEntity<AdminVendorDto> updateVendorStatus(
            @PathVariable String id,
            @RequestParam Boolean isActive) {
        try {
            log.info("Updating vendor status: {} - {}", id, isActive);
            AdminVendorDto vendor = adminService.updateVendorStatus(id, isActive);
            return ResponseEntity.ok(vendor);
        } catch (Exception e) {
            log.error("Error updating vendor status: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Delete vendor
     * DELETE /api/admin/vendors/{id}
     */
    @DeleteMapping("/vendors/{id}")
    public ResponseEntity<Void> deleteVendor(@PathVariable String id) {
        try {
            log.info("Deleting vendor: {}", id);
            adminService.deleteVendor(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error deleting vendor: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // ==================== Order Management ====================

    /**
     * Get all orders
     * GET /api/admin/orders
     */
    @GetMapping("/orders")
    public ResponseEntity<List<AdminOrderDto>> getAllOrders() {
        try {
            log.info("Fetching all orders");
            List<AdminOrderDto> orders = adminService.getAllOrders();
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            log.error("Error fetching orders: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get order by ID
     * GET /api/admin/orders/{id}
     */
    @GetMapping("/orders/{id}")
    public ResponseEntity<AdminOrderDto> getOrderById(@PathVariable String id) {
        try {
            AdminOrderDto order = adminService.getOrderById(id);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            log.error("Error fetching order: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Update order status
     * PUT /api/admin/orders/{id}/status
     */
    @PutMapping("/orders/{id}/status")
    public ResponseEntity<AdminOrderDto> updateOrderStatus(
            @PathVariable String id,
            @RequestParam String status) {
        try {
            log.info("Updating order status: {} - {}", id, status);
            AdminOrderDto order = adminService.updateOrderStatus(id, status);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            log.error("Error updating order status: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Delete order
     * DELETE /api/admin/orders/{id}
     */
    @DeleteMapping("/orders/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable String id) {
        try {
            log.info("Deleting order: {}", id);
            adminService.deleteOrder(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error deleting order: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // ==================== Bid Management ====================

    /**
     * Get all bids
     * GET /api/admin/bids
     */
    @GetMapping("/bids")
    public ResponseEntity<List<AdminBidDto>> getAllBids() {
        try {
            log.info("Fetching all bids");
            List<AdminBidDto> bids = adminService.getAllBids();
            return ResponseEntity.ok(bids);
        } catch (Exception e) {
            log.error("Error fetching bids: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get bid by ID
     * GET /api/admin/bids/{id}
     */
    @GetMapping("/bids/{id}")
    public ResponseEntity<AdminBidDto> getBidById(@PathVariable String id) {
        try {
            AdminBidDto bid = adminService.getBidById(id);
            return ResponseEntity.ok(bid);
        } catch (Exception e) {
            log.error("Error fetching bid: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Delete bid
     * DELETE /api/admin/bids/{id}
     */
    @DeleteMapping("/bids/{id}")
    public ResponseEntity<Void> deleteBid(@PathVariable String id) {
        try {
            log.info("Deleting bid: {}", id);
            adminService.deleteBid(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error deleting bid: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // ==================== Menu Item Management ====================

    /**
     * Get all menu items
     * GET /api/admin/menu-items
     */
    @GetMapping("/menu-items")
    public ResponseEntity<List<AdminMenuItemDto>> getAllMenuItems() {
        try {
            log.info("Fetching all menu items");
            List<AdminMenuItemDto> menuItems = adminService.getAllMenuItems();
            return ResponseEntity.ok(menuItems);
        } catch (Exception e) {
            log.error("Error fetching menu items: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get menu item by ID
     * GET /api/admin/menu-items/{id}
     */
    @GetMapping("/menu-items/{id}")
    public ResponseEntity<AdminMenuItemDto> getMenuItemById(@PathVariable String id) {
        try {
            AdminMenuItemDto menuItem = adminService.getMenuItemById(id);
            return ResponseEntity.ok(menuItem);
        } catch (Exception e) {
            log.error("Error fetching menu item: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Delete menu item
     * DELETE /api/admin/menu-items/{id}
     */
    @DeleteMapping("/menu-items/{id}")
    public ResponseEntity<Void> deleteMenuItem(@PathVariable String id) {
        try {
            log.info("Deleting menu item: {}", id);
            adminService.deleteMenuItem(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error deleting menu item: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // ==================== Payment Management ====================

    /**
     * Get all payments
     * GET /api/admin/payments
     */
    @GetMapping("/payments")
    public ResponseEntity<List<AdminPaymentDto>> getAllPayments() {
        try {
            log.info("Fetching all payments");
            List<AdminPaymentDto> payments = adminService.getAllPayments();
            return ResponseEntity.ok(payments);
        } catch (Exception e) {
            log.error("Error fetching payments: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get payment by ID
     * GET /api/admin/payments/{id}
     */
    @GetMapping("/payments/{id}")
    public ResponseEntity<AdminPaymentDto> getPaymentById(@PathVariable String id) {
        try {
            AdminPaymentDto payment = adminService.getPaymentById(id);
            return ResponseEntity.ok(payment);
        } catch (Exception e) {
            log.error("Error fetching payment: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
}

