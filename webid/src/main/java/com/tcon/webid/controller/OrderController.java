package com.tcon.webid.controller;

import com.tcon.webid.dto.OrderRequestDto;
import com.tcon.webid.entity.Order;
import com.tcon.webid.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/vendor/{vendorOrganizationId}/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping
    public Order createOrder(@PathVariable String vendorOrganizationId,
                             @RequestBody OrderRequestDto dto,
                             @RequestParam List<String> vendorIds) {
        // vendorIds: vendors selected for bidding (from UI)
        return orderService.createOrder(dto, vendorIds);
    }

    @GetMapping("/{orderId}")
    public Order getOrder(@PathVariable String vendorOrganizationId, @PathVariable String orderId) {
        Order order = orderService.getOrderById(orderId);
        if (order.getVendorOrganizationId() != null &&
                !order.getVendorOrganizationId().equals(vendorOrganizationId)) {
            throw new IllegalArgumentException("Access denied");
        }
        return order;
    }

    @GetMapping
    public List<Order> getOrdersByVendor(@PathVariable String vendorOrganizationId) {
        return orderService.getOrdersByVendor(vendorOrganizationId);
    }

    @PutMapping("/{orderId}/status")
    public Order updateOrderStatus(@PathVariable String vendorOrganizationId,
                                   @PathVariable String orderId,
                                   @RequestParam String status) {
        Order order = orderService.getOrderById(orderId);
        if (!order.getVendorOrganizationId().equals(vendorOrganizationId)) {
            throw new IllegalArgumentException("Access denied");
        }
        return orderService.updateOrderStatus(orderId, status);
    }

    @DeleteMapping("/{orderId}")
    public void deleteOrder(@PathVariable String vendorOrganizationId, @PathVariable String orderId) {
        Order order = orderService.getOrderById(orderId);
        if (!order.getVendorOrganizationId().equals(vendorOrganizationId)) {
            throw new IllegalArgumentException("Access denied");
        }
        orderService.deleteOrder(orderId);
    }
}
