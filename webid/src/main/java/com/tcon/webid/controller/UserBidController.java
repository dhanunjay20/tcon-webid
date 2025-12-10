package com.tcon.webid.controller;

import com.tcon.webid.entity.Bid;
import com.tcon.webid.entity.Order;
import com.tcon.webid.service.BidService;
import com.tcon.webid.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/user/{userId}/bids")
public class UserBidController {

    @Autowired
    private BidService bidService;

    @Autowired
    private OrderService orderService;

    /**
     * Get all bids for a specific order (user must own the order)
     */
    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<Bid>> getBidsForOrder(
            @PathVariable String userId,
            @PathVariable String orderId) {

        log.debug("Get bids for order {} by user {}", orderId, userId);

        // Verify the user owns this order
        Order order = orderService.getOrderById(orderId);
        if (order == null) {
            log.warn("Order {} not found", orderId);
            return ResponseEntity.notFound().build();
        }

        if (!order.getCustomerId().equals(userId)) {
            log.warn("User {} attempted to access order {} owned by {}", userId, orderId, order.getCustomerId());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<Bid> bids = bidService.getBidsByOrder(orderId);
        return ResponseEntity.ok(bids);
    }

    /**
     * Get all bids across all user's orders
     */
    @GetMapping
    public ResponseEntity<List<Bid>> getAllUserBids(@PathVariable String userId) {
        log.debug("Get all bids for user {}", userId);

        // Get all user orders
        List<Order> userOrders = orderService.getOrdersByCustomer(userId);

        // Get bids for all orders
        List<Bid> allBids = userOrders.stream()
                .flatMap(order -> bidService.getBidsByOrder(order.getId()).stream())
                .toList();

        return ResponseEntity.ok(allBids);
    }

    /**
     * Get a specific bid (user must own the associated order)
     */
    @GetMapping("/{bidId}")
    public ResponseEntity<Bid> getBid(
            @PathVariable String userId,
            @PathVariable String bidId) {

        log.debug("Get bid {} for user {}", bidId, userId);

        Bid bid = bidService.getBidById(bidId);
        if (bid == null) {
            log.warn("Bid {} not found", bidId);
            return ResponseEntity.notFound().build();
        }

        // Verify the user owns the order associated with this bid
        Order order = orderService.getOrderById(bid.getOrderId());
        if (order == null || !order.getCustomerId().equals(userId)) {
            log.warn("User {} attempted to access bid {} for order owned by {}",
                    userId, bidId, order != null ? order.getCustomerId() : "unknown");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(bid);
    }

    /**
     * Accept a bid for an order
     */
    @PutMapping("/{bidId}/accept")
    public ResponseEntity<Bid> acceptBid(
            @PathVariable String userId,
            @PathVariable String bidId) {

        log.info("User {} accepting bid {}", userId, bidId);

        Bid bid = bidService.getBidById(bidId);
        if (bid == null) {
            log.warn("Bid {} not found", bidId);
            return ResponseEntity.notFound().build();
        }

        // Verify the user owns the order
        Order order = orderService.getOrderById(bid.getOrderId());
        if (order == null || !order.getCustomerId().equals(userId)) {
            log.warn("User {} attempted to accept bid {} for order owned by {}",
                    userId, bidId, order != null ? order.getCustomerId() : "unknown");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Allow accepting 'quoted' or 'pending' bids. Vendors submit quotes as 'quoted'.
        String status = bid.getStatus();
        if (!"pending".equalsIgnoreCase(status) && !"quoted".equalsIgnoreCase(status)) {
            log.warn("Bid {} has status {} and cannot be accepted", bidId, status);
            return ResponseEntity.badRequest().build();
        }

        Bid acceptedBid = bidService.acceptBid(bidId);
        return ResponseEntity.ok(acceptedBid);
    }

    /**
     * Reject a bid for an order
     */
    @PutMapping("/{bidId}/reject")
    public ResponseEntity<?> rejectBid(
            @PathVariable String userId,
            @PathVariable String bidId) {

        log.info("User {} rejecting bid {}", userId, bidId);

        Bid bid = bidService.getBidById(bidId);
        if (bid == null) {
            log.warn("Bid {} not found", bidId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(java.util.Map.of("error", "Bid not found"));
        }

        // Verify the user owns the order
        Order order = orderService.getOrderById(bid.getOrderId());
        if (order == null || !order.getCustomerId().equals(userId)) {
            log.warn("User {} attempted to reject bid {} for order owned by {}",
                    userId, bidId, order != null ? order.getCustomerId() : "unknown");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(java.util.Map.of("error", "Access denied"));
        }

        // Allow rejecting 'pending' or 'quoted' bids.
        String status = bid.getStatus();
        if (!"pending".equalsIgnoreCase(status) && !"quoted".equalsIgnoreCase(status)) {
            log.warn("Bid {} has status {} and cannot be rejected", bidId, bid.getStatus());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(java.util.Map.of("error", "Bid must be pending or quoted to reject"));
        }

        Bid rejectedBid = bidService.rejectBid(bidId);
        return ResponseEntity.ok(rejectedBid);
    }

    /**
     * Get pending bids for a specific order
     */
    @GetMapping("/order/{orderId}/pending")
    public ResponseEntity<List<Bid>> getPendingBidsForOrder(
            @PathVariable String userId,
            @PathVariable String orderId) {

        log.debug("Get pending bids for order {} by user {}", orderId, userId);

        // Verify the user owns this order
        Order order = orderService.getOrderById(orderId);
        if (order == null) {
            log.warn("Order {} not found", orderId);
            return ResponseEntity.notFound().build();
        }

        if (!order.getCustomerId().equals(userId)) {
            log.warn("User {} attempted to access order {} owned by {}", userId, orderId, order.getCustomerId());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<Bid> bids = bidService.getBidsByOrder(orderId).stream()
                .filter(bid -> "pending".equalsIgnoreCase(bid.getStatus()))
                .toList();

        return ResponseEntity.ok(bids);
    }

    /**
     * Get accepted bid for a specific order
     */
    @GetMapping("/order/{orderId}/accepted")
    public ResponseEntity<Bid> getAcceptedBidForOrder(
            @PathVariable String userId,
            @PathVariable String orderId) {

        log.debug("Get accepted bid for order {} by user {}", orderId, userId);

        // Verify the user owns this order
        Order order = orderService.getOrderById(orderId);
        if (order == null) {
            log.warn("Order {} not found", orderId);
            return ResponseEntity.notFound().build();
        }

        if (!order.getCustomerId().equals(userId)) {
            log.warn("User {} attempted to access order {} owned by {}", userId, orderId, order.getCustomerId());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<Bid> acceptedBids = bidService.getBidsByOrder(orderId).stream()
                .filter(bid -> "accepted".equalsIgnoreCase(bid.getStatus()))
                .toList();

        if (acceptedBids.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(acceptedBids.get(0));
    }

    /**
     * Compare all bids for an order (sorted by price)
     */
    @GetMapping("/order/{orderId}/compare")
    public ResponseEntity<List<Bid>> compareBidsForOrder(
            @PathVariable String userId,
            @PathVariable String orderId) {

        log.debug("Compare bids for order {} by user {}", orderId, userId);

        // Verify the user owns this order
        Order order = orderService.getOrderById(orderId);
        if (order == null) {
            log.warn("Order {} not found", orderId);
            return ResponseEntity.notFound().build();
        }

        if (!order.getCustomerId().equals(userId)) {
            log.warn("User {} attempted to access order {} owned by {}", userId, orderId, order.getCustomerId());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Get all submitted bids sorted by price
        List<Bid> bids = bidService.getBidsByOrder(orderId).stream()
                .filter(bid -> "pending".equalsIgnoreCase(bid.getStatus()) ||
                              "quoted".equalsIgnoreCase(bid.getStatus()) ||
                              "accepted".equalsIgnoreCase(bid.getStatus()))
                .sorted((b1, b2) -> Double.compare(
                        b1.getProposedTotalPrice(),
                        b2.getProposedTotalPrice()
                ))
                .toList();

        return ResponseEntity.ok(bids);
    }
}
