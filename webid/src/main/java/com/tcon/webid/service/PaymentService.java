package com.tcon.webid.service;

import com.tcon.webid.dto.*;
import com.tcon.webid.entity.Payment;

import java.util.List;

public interface PaymentService {

    /**
     * Create a payment intent for an order
     * @param request Payment intent request details
     * @param customerId Customer ID
     * @param idempotencyKey Optional idempotency key for safe retries (can be null)
     * @return Payment intent response with client secret
     */
    PaymentIntentResponseDto createPaymentIntent(PaymentIntentRequestDto request, String customerId, String idempotencyKey);

    /**
     * Confirm a payment after successful payment on client side
     */
    PaymentResponseDto confirmPayment(String paymentIntentId);

    /**
     * Get payment by ID
     */
    PaymentResponseDto getPaymentById(String paymentId);

    /**
     * Get payment by payment intent ID
     */
    PaymentResponseDto getPaymentByPaymentIntentId(String paymentIntentId);

    /**
     * Get all payments for an order
     */
    List<PaymentResponseDto> getPaymentsByOrderId(String orderId);

    /**
     * Get all payments for a customer
     */
    List<PaymentResponseDto> getPaymentsByCustomerId(String customerId);

    /**
     * Get all payments for a vendor
     */
    List<PaymentResponseDto> getPaymentsByVendorId(String vendorOrganizationId);

    /**
     * Cancel a payment intent
     */
    PaymentResponseDto cancelPayment(String paymentId);

    /**
     * Refund a payment (full or partial)
     */
    PaymentResponseDto refundPayment(RefundRequestDto request);

    /**
     * Handle Stripe webhook events
     */
    void handleWebhookEvent(String payload, String signatureHeader);

    /**
     * Get or create Stripe customer
     */
    String getOrCreateStripeCustomer(String customerId, String email, String name);
}

