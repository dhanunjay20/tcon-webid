package com.tcon.webid.controller;

import com.tcon.webid.dto.OrderRequestDto;
import com.tcon.webid.dto.OrderResponseDto;
import com.tcon.webid.entity.Order;
import com.tcon.webid.service.OrderService;
import com.tcon.webid.mapper.OrderResponseMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/user/{userId}/orders")
public class UserOrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderResponseMapper orderResponseMapper;

    /**
     * Create a new order for the user
     * @param userId The user ID from the path
     * @param dto Order details
     * @param vendorIds List of vendor IDs to send bid requests to
     * @return The created Order
     */
    @PostMapping
    public ResponseEntity<Order> createOrder(@PathVariable String userId,
                                             @RequestBody OrderRequestDto dto,
                                             @RequestParam(required = false) List<String> vendorIds) {
        log.info("Create order request for user: {}", userId);

        // Ensure the customerId in the DTO matches the userId from path
        dto.setCustomerId(userId);

        Order order = orderService.createOrder(dto, vendorIds);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    /**
     * Get all orders for a specific user
     * @param userId The user ID
     * @return List of orders
     */
    @GetMapping
    public ResponseEntity<List<OrderResponseDto>> getUserOrders(@PathVariable String userId) {
        log.debug("Get orders request for user: {}", userId);
        List<Order> orders = orderService.getOrdersByCustomer(userId);
        List<OrderResponseDto> dtos = orders.stream().map(orderResponseMapper::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    /**
     * Get a specific order by ID (if it belongs to the user)
     * @param userId The user ID
     * @param orderId The order ID
     * @return The order
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponseDto> getOrder(@PathVariable String userId,
                                         @PathVariable String orderId) {
        log.debug("Get order {} request for user: {}", orderId, userId);
        Order order = orderService.getOrderById(orderId);

        // Verify the order belongs to this user
        if (!order.getCustomerId().equals(userId)) {
            log.warn("User {} attempted to access order {} belonging to user {}",
                    userId, orderId, order.getCustomerId());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(orderResponseMapper.toDto(order));
    }

    /**
     * Update order status (user can cancel their order)
     * @param userId The user ID
     * @param orderId The order ID
     * @param status The new status
     * @return The updated order
     */
    @PutMapping("/{orderId}/status")
    public ResponseEntity<Order> updateOrderStatus(@PathVariable String userId,
                                                   @PathVariable String orderId,
                                                   @RequestParam String status) {
        log.info("Update order {} status to {} for user: {}", orderId, status, userId);
        Order order = orderService.getOrderById(orderId);

        // Verify the order belongs to this user
        if (!order.getCustomerId().equals(userId)) {
            log.warn("User {} attempted to update order {} belonging to user {}",
                    userId, orderId, order.getCustomerId());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Users can only cancel their own orders
        if (!"cancelled".equalsIgnoreCase(status)) {
            log.warn("User {} attempted to set invalid status {} for order {}",
                    userId, status, orderId);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        Order updatedOrder = orderService.updateOrderStatus(orderId, status);
        return ResponseEntity.ok(updatedOrder);
    }

    /**
     * Delete an order (if it's in pending status)
     * @param userId The user ID
     * @param orderId The order ID
     * @return No content
     */
    @DeleteMapping("/{orderId}")
    public ResponseEntity<Void> deleteOrder(@PathVariable String userId,
                                           @PathVariable String orderId) {
        log.info("Delete order {} request for user: {}", orderId, userId);
        Order order = orderService.getOrderById(orderId);

        // Verify the order belongs to this user
        if (!order.getCustomerId().equals(userId)) {
            log.warn("User {} attempted to delete order {} belonging to user {}",
                    userId, orderId, order.getCustomerId());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Only allow deletion of pending orders
        if (!"pending".equalsIgnoreCase(order.getStatus())) {
            log.warn("User {} attempted to delete order {} with status {}",
                    userId, orderId, order.getStatus());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        orderService.deleteOrder(orderId);
        return ResponseEntity.noContent().build();
    }
}
