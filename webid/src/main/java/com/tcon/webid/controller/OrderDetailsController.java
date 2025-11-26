package com.tcon.webid.controller;

import com.tcon.webid.dto.OrderResponseDto;
import com.tcon.webid.entity.Order;
import com.tcon.webid.service.OrderService;
import com.tcon.webid.mapper.OrderResponseMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
public class OrderDetailsController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderResponseMapper orderResponseMapper;

    // User-visible endpoint
    @GetMapping("/api/user/{userId}/orders/{orderId}/details")
    public OrderResponseDto getUserOrderDetails(@PathVariable String userId, @PathVariable String orderId) {
        log.debug("Get order details {} for user {}", orderId, userId);
        Order order = orderService.getOrderById(orderId);
        if (!order.getCustomerId().equals(userId)) {
            throw new IllegalArgumentException("Access denied");
        }
        return orderResponseMapper.toDto(order);
    }

    // Vendor-visible endpoint (vendor must have a bid or be assigned)
    @GetMapping("/api/vendor/{vendorOrganizationId}/orders/{orderId}/details")
    public OrderResponseDto getVendorOrderDetails(@PathVariable String vendorOrganizationId, @PathVariable String orderId) {
        log.debug("Get order details {} for vendor {}", orderId, vendorOrganizationId);
        // Basic check: vendor must have a bid for this order OR be the assigned vendor
        Order order = orderService.getOrderById(orderId);
        boolean hasBid = false;
        try {
            hasBid = orderService.getOrdersByVendor(vendorOrganizationId).stream().anyMatch(o -> o.getId().equals(orderId));
        } catch (Exception e) {
            // fallback
        }

        if (!hasBid && (order.getVendorOrganizationId() == null || !order.getVendorOrganizationId().equals(vendorOrganizationId))) {
            throw new IllegalArgumentException("Access denied");
        }

        return orderResponseMapper.toDto(order);
    }
}
