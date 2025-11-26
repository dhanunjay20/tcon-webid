package com.tcon.webid.controller;

import com.tcon.webid.dto.OrderRequestDto;
import com.tcon.webid.dto.OrderResponseDto;
import com.tcon.webid.entity.Bid;
import com.tcon.webid.entity.Order;
import com.tcon.webid.service.BidService;
import com.tcon.webid.service.OrderService;
import com.tcon.webid.mapper.OrderResponseMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/vendor/{vendorOrganizationId}/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private BidService bidService;

    @Autowired
    private OrderResponseMapper orderResponseMapper;

    @PostMapping
    public Order createOrder(@PathVariable String vendorOrganizationId,
                             @RequestBody OrderRequestDto dto,
                             @RequestParam(required = false) List<String> vendorIds) {
        // vendorIds: vendors selected for bidding (from UI)
        return orderService.createOrder(dto, vendorIds);
    }

    @GetMapping("/{orderId}")
    public OrderResponseDto getOrder(@PathVariable String vendorOrganizationId, @PathVariable String orderId) {
        Order order = orderService.getOrderById(orderId);

        // Check if vendor has a bid for this order
        List<Bid> vendorBids = bidService.getBidsByVendor(vendorOrganizationId);
        boolean hasAccess = vendorBids.stream()
                .anyMatch(bid -> bid.getOrderId().equals(orderId));

        if (!hasAccess) {
            throw new IllegalArgumentException("Access denied - no bid found for this order");
        }

        return orderResponseMapper.toDto(order);
    }

    @GetMapping
    public List<OrderResponseDto> getOrdersByVendor(@PathVariable String vendorOrganizationId) {
        // Get all bids for this vendor
        List<Bid> vendorBids = bidService.getBidsByVendor(vendorOrganizationId);

        // Extract unique order IDs from bids
        List<String> orderIds = vendorBids.stream()
                .map(Bid::getOrderId)
                .distinct()
                .collect(Collectors.toList());

        // Fetch orders by IDs
        if (orderIds.isEmpty()) {
            return List.of(); // Return empty list if no bids
        }

        List<Order> orders = orderService.getOrdersByIds(orderIds);
        return orders.stream().map(orderResponseMapper::toDto).collect(Collectors.toList());
    }

    @PutMapping("/{orderId}/status")
    public Order updateOrderStatus(@PathVariable String vendorOrganizationId,
                                   @PathVariable String orderId,
                                   @RequestParam String status) {
        Order order = orderService.getOrderById(orderId);

        // Verify vendor has a bid for this order
        List<Bid> vendorBids = bidService.getBidsByVendor(vendorOrganizationId);
        boolean hasAccess = vendorBids.stream()
                .anyMatch(bid -> bid.getOrderId().equals(orderId));

        if (!hasAccess) {
            throw new IllegalArgumentException("Access denied - no bid found for this order");
        }

        // Only the accepted vendor (assigned on the order) may update order status
        if (order.getVendorOrganizationId() == null || !order.getVendorOrganizationId().equals(vendorOrganizationId)) {
            throw new IllegalArgumentException("Access denied - only accepted vendor can update order status");
        }

        return orderService.updateOrderStatus(orderId, status);
    }

    @DeleteMapping("/{orderId}")
    public void deleteOrder(@PathVariable String vendorOrganizationId, @PathVariable String orderId) {
        Order order = orderService.getOrderById(orderId);

        // Verify vendor has a bid for this order
        List<Bid> vendorBids = bidService.getBidsByVendor(vendorOrganizationId);
        boolean hasAccess = vendorBids.stream()
                .anyMatch(bid -> bid.getOrderId().equals(orderId));

        if (!hasAccess) {
            throw new IllegalArgumentException("Access denied - no bid found for this order");
        }

        orderService.deleteOrder(orderId);
    }
}