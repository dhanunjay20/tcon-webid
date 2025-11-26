package com.tcon.webid.service;

import com.tcon.webid.dto.OrderRequestDto;
import com.tcon.webid.entity.Order;
import java.util.List;

/**
 * Service for order management.
 * Handles order submission (with multi-vendor bid requests), order status, and CRUD operations.
 */
public interface OrderService {
    /**
     * Create an order for an event and send bid requests to the given vendors.
     * @param dto Order details (event/menu/user info)
     * @param vendorOrganizationIds List of vendor org IDs to send requests/bids
     * @return The created Order entity
     */

    Order createOrder(OrderRequestDto dto, List<String> vendorOrganizationIds);

    Order getOrderById(String id);

    List<Order> getOrdersByCustomer(String customerId);

    /**
     * Get all orders assigned/confirmed to a given vendor.
     */
    List<Order> getOrdersByVendor(String vendorOrganizationId);

    /**
     * Update order status (e.g., "confirmed", "in_progress", "completed")
     */
    Order updateOrderStatus(String id, String newStatus);

    void deleteOrder(String id);
}
