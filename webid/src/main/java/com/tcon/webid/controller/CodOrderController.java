package com.tcon.webid.controller;

import com.tcon.webid.dto.OrderRequestDto;
import com.tcon.webid.entity.Order;
import com.tcon.webid.entity.Payment;
import com.tcon.webid.service.OrderService;
import com.tcon.webid.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controller to handle Cash-On-Delivery (COD) orders and their payment lifecycle.
 */
@Slf4j
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class CodOrderController {

    private final OrderService orderService;
    private final PaymentRepository paymentRepository;

    /**
     * Create a new order with Cash-On-Delivery payment. This will create an Order and a Payment record
     * with status "cod_pending".
     *
     * POST /api/orders/cod
     */
    @PostMapping("/cod")
    public ResponseEntity<Map<String,Object>> createCodOrder(@RequestBody OrderRequestDto dto,
                                                             @RequestParam(required = false) List<String> vendorIds) {
        try {
            Order order = orderService.createOrder(dto, vendorIds);

            // Create payment record for COD
            Payment payment = new Payment();
            payment.setOrderId(order.getId());
            payment.setCustomerId(order.getCustomerId());
            long amountInCents = (long) Math.round(order.getTotalPrice() * 100);
            payment.setAmountInCents(amountInCents);
            payment.setAmount(order.getTotalPrice());
            payment.setCurrency("USD");
            payment.setStatus("cod_pending");
            payment.setPaymentMethod("cod");
            payment.setDescription("COD for order " + order.getId());
            payment.setCreatedAt(Instant.now());
            payment.setUpdatedAt(Instant.now());

            Payment saved = paymentRepository.save(payment);

            Map<String,Object> resp = new HashMap<>();
            resp.put("paymentId", saved.getId());
            resp.put("orderId", saved.getOrderId());
            resp.put("status", saved.getStatus());
            resp.put("paymentMethod", saved.getPaymentMethod());
            resp.put("amount", saved.getAmount());
            resp.put("message", "COD order and payment created");

            return ResponseEntity.status(HttpStatus.CREATED).body(resp);
        } catch (Exception e) {
            log.error("Error creating COD order: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to create COD order: " + e.getMessage()));
        }
    }

    /**
     * Create COD payment for an existing order (if you already created the order separately)
     * POST /api/orders/{orderId}/cod
     */
    @PostMapping("/{orderId}/cod")
    public ResponseEntity<Map<String,Object>> createCodForExistingOrder(@PathVariable String orderId) {
        try {
            // Load existing order (orderService.getOrderById throws if not found)
            Order order = orderService.getOrderById(orderId);

            // Create payment record for COD
            Payment payment = new Payment();
            payment.setOrderId(order.getId());
            payment.setCustomerId(order.getCustomerId());
            long amountInCents = (long) Math.round(order.getTotalPrice() * 100);
            payment.setAmountInCents(amountInCents);
            payment.setAmount(order.getTotalPrice());
            payment.setCurrency("USD");
            payment.setStatus("cod_pending");
            payment.setPaymentMethod("cod");
            payment.setDescription("COD for order " + order.getId());
            payment.setCreatedAt(Instant.now());
            payment.setUpdatedAt(Instant.now());

            Payment saved = paymentRepository.save(payment);

            Map<String,Object> resp = new HashMap<>();
            resp.put("paymentId", saved.getId());
            resp.put("orderId", saved.getOrderId());
            resp.put("status", saved.getStatus());
            resp.put("paymentMethod", saved.getPaymentMethod());
            resp.put("amount", saved.getAmount());
            resp.put("message", "COD payment created");

            return ResponseEntity.status(HttpStatus.CREATED).body(resp);
        } catch (Exception e) {
            log.error("Error creating COD payment for order {}: {}", orderId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to create COD payment: " + e.getMessage()));
        }
    }

    /**
     * Get COD payment status for an order
     * GET /api/orders/{orderId}/cod/status
     */
    @GetMapping("/{orderId}/cod/status")
    public ResponseEntity<Map<String, Object>> getCodPaymentStatus(@PathVariable String orderId) {
        List<Payment> payments = paymentRepository.findByOrderId(orderId);
        if (payments == null || payments.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "No payment found for order"));
        }

        // Prefer non-stripe (cod) payments if present
        Optional<Payment> codPayment = payments.stream()
                .filter(p -> p.getStatus() != null && p.getStatus().startsWith("cod"))
                .findFirst();

        Payment payment = codPayment.orElse(payments.get(0));

        Map<String, Object> resp = new HashMap<>();
        resp.put("paymentId", payment.getId());
        resp.put("status", payment.getStatus());
        resp.put("amountInCents", payment.getAmountInCents());
        resp.put("currency", payment.getCurrency());
        resp.put("paidAt", payment.getPaidAt());
        return ResponseEntity.ok(resp);
    }

    /**
     * Mark COD payment as collected (called by vendor/staff when cash is collected)
     * POST /api/orders/{orderId}/cod/collect
     */
    @PostMapping("/{orderId}/cod/collect")
    public ResponseEntity<Map<String, Object>> collectCodPayment(@PathVariable String orderId) {
        try {
            Optional<Payment> opt = paymentRepository.findByOrderIdAndStatus(orderId, "cod_pending");
            if (opt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "No pending COD payment found for this order"));
            }

            Payment payment = opt.get();
            payment.setStatus("succeeded");
            payment.setPaidAt(Instant.now());
            payment.setUpdatedAt(Instant.now());
            paymentRepository.save(payment);

            // Update order status to confirmed
            orderService.updateOrderStatus(orderId, "confirmed");

            Map<String, Object> resp = new HashMap<>();
            resp.put("paymentId", payment.getId());
            resp.put("status", payment.getStatus());
            resp.put("paidAt", payment.getPaidAt());
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            log.error("Failed to collect COD payment for order {}: {}", orderId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Failed to collect COD payment: " + e.getMessage()));
        }
    }

    /**
     * Cancel COD payment and order
     * POST /api/orders/{orderId}/cod/cancel
     */
    @PostMapping("/{orderId}/cod/cancel")
    public ResponseEntity<Map<String, Object>> cancelCodPayment(@PathVariable String orderId) {
        try {
            Optional<Payment> opt = paymentRepository.findByOrderIdAndStatus(orderId, "cod_pending");
            if (opt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "No pending COD payment found for this order"));
            }

            Payment payment = opt.get();
            payment.setStatus("cancelled");
            payment.setUpdatedAt(Instant.now());
            paymentRepository.save(payment);

            orderService.updateOrderStatus(orderId, "cancelled");

            return ResponseEntity.ok(Map.of("message", "COD payment and order cancelled"));
        } catch (Exception e) {
            log.error("Failed to cancel COD payment for order {}: {}", orderId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Failed to cancel COD payment: " + e.getMessage()));
        }
    }
}
