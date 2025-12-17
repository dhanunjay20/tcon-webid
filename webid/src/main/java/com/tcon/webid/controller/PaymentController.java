package com.tcon.webid.controller;

import com.tcon.webid.dto.*;
import com.tcon.webid.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * Create a payment intent
     * POST /api/payments/create-intent
     */
    @PostMapping("/create-intent")
    public ResponseEntity<PaymentIntentResponseDto> createPaymentIntent(
            @Valid @RequestBody PaymentIntentRequestDto request,
            Authentication authentication) {
        try {
            // Get customer ID from authentication (assuming userId is stored in principal)
            String customerId = authentication != null ? authentication.getName() : request.getCustomerEmail();

            PaymentIntentResponseDto response = paymentService.createPaymentIntent(request, customerId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error creating payment intent: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(PaymentIntentResponseDto.builder()
                            .message("Failed to create payment intent: " + e.getMessage())
                            .build());
        }
    }

    /**
     * Confirm a payment after client-side payment succeeds
     * POST /api/payments/confirm/{paymentIntentId}
     */
    @PostMapping("/confirm/{paymentIntentId}")
    public ResponseEntity<PaymentResponseDto> confirmPayment(@PathVariable String paymentIntentId) {
        try {
            PaymentResponseDto response = paymentService.confirmPayment(paymentIntentId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error confirming payment: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Get payment by ID
     * GET /api/payments/{paymentId}
     */
    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponseDto> getPaymentById(@PathVariable String paymentId) {
        try {
            PaymentResponseDto response = paymentService.getPaymentById(paymentId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error retrieving payment: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Get payment by payment intent ID
     * GET /api/payments/intent/{paymentIntentId}
     */
    @GetMapping("/intent/{paymentIntentId}")
    public ResponseEntity<PaymentResponseDto> getPaymentByPaymentIntentId(@PathVariable String paymentIntentId) {
        try {
            PaymentResponseDto response = paymentService.getPaymentByPaymentIntentId(paymentIntentId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error retrieving payment by intent: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Get all payments for an order
     * GET /api/payments/order/{orderId}
     */
    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<PaymentResponseDto>> getPaymentsByOrderId(@PathVariable String orderId) {
        try {
            List<PaymentResponseDto> payments = paymentService.getPaymentsByOrderId(orderId);
            return ResponseEntity.ok(payments);
        } catch (Exception e) {
            log.error("Error retrieving payments for order: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get all payments for a customer
     * GET /api/payments/customer/{customerId}
     */
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<PaymentResponseDto>> getPaymentsByCustomerId(@PathVariable String customerId) {
        try {
            List<PaymentResponseDto> payments = paymentService.getPaymentsByCustomerId(customerId);
            return ResponseEntity.ok(payments);
        } catch (Exception e) {
            log.error("Error retrieving payments for customer: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get all payments for a vendor
     * GET /api/payments/vendor/{vendorOrganizationId}
     */
    @GetMapping("/vendor/{vendorOrganizationId}")
    public ResponseEntity<List<PaymentResponseDto>> getPaymentsByVendorId(@PathVariable String vendorOrganizationId) {
        try {
            List<PaymentResponseDto> payments = paymentService.getPaymentsByVendorId(vendorOrganizationId);
            return ResponseEntity.ok(payments);
        } catch (Exception e) {
            log.error("Error retrieving payments for vendor: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Cancel a payment
     * POST /api/payments/{paymentId}/cancel
     */
    @PostMapping("/{paymentId}/cancel")
    public ResponseEntity<PaymentResponseDto> cancelPayment(@PathVariable String paymentId) {
        try {
            PaymentResponseDto response = paymentService.cancelPayment(paymentId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error cancelling payment: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Refund a payment
     * POST /api/payments/refund
     */
    @PostMapping("/refund")
    public ResponseEntity<PaymentResponseDto> refundPayment(@Valid @RequestBody RefundRequestDto request) {
        try {
            PaymentResponseDto response = paymentService.refundPayment(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error processing refund: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Stripe webhook endpoint
     * POST /api/payments/webhook
     */
    @PostMapping("/webhook")
    public ResponseEntity<Map<String, String>> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String signatureHeader) {
        try {
            log.info("Received webhook event");
            paymentService.handleWebhookEvent(payload, signatureHeader);

            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error processing webhook: {}", e.getMessage());
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * Health check endpoint
     * GET /api/payments/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "healthy");
        response.put("service", "payment");
        return ResponseEntity.ok(response);
    }
}

