package com.tcon.webid.service;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.net.ApiResource;
import com.stripe.net.Webhook;
import com.stripe.param.*;
import com.tcon.webid.config.StripeConfig;
import com.tcon.webid.dto.*;
import com.tcon.webid.entity.Order;
import com.tcon.webid.entity.Payment;
import com.tcon.webid.entity.User;
import com.tcon.webid.exception.PaymentException;
import com.tcon.webid.exception.ResourceNotFoundException;
import com.tcon.webid.repository.OrderRepository;
import com.tcon.webid.repository.PaymentRepository;
import com.tcon.webid.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final StripeConfig stripeConfig;

    @Override
    @Transactional
    public PaymentIntentResponseDto createPaymentIntent(PaymentIntentRequestDto request, String customerId) {
        try {
            log.info("Creating payment intent for order: {} and customer: {}", request.getOrderId(), customerId);

            // Validate order exists
            Order order = orderRepository.findById(request.getOrderId())
                    .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + request.getOrderId()));

            // Validate customer
            User user = userRepository.findById(customerId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + customerId));

            // Check if payment already exists for this order
            paymentRepository.findByOrderIdAndStatus(request.getOrderId(), "succeeded")
                    .ifPresent(payment -> {
                        throw new PaymentException("Payment already completed for this order", "PAYMENT_ALREADY_EXISTS");
                    });

            // Get or create Stripe customer
            String stripeCustomerId = getOrCreateStripeCustomer(
                    customerId,
                    request.getCustomerEmail() != null ? request.getCustomerEmail() : user.getEmail(),
                    request.getCustomerName() != null ? request.getCustomerName() : user.getFirstName() + " " + user.getLastName()
            );

            // Convert amount to cents (Stripe uses smallest currency unit)
            long amountInCents = (long) (request.getAmount() * 100);

            // Create metadata
            Map<String, String> metadata = new HashMap<>();
            metadata.put("orderId", request.getOrderId());
            metadata.put("customerId", customerId);
            metadata.put("vendorOrganizationId", order.getVendorOrganizationId() != null ? order.getVendorOrganizationId() : "");

            // Create payment intent parameters
            PaymentIntentCreateParams.Builder paramsBuilder = PaymentIntentCreateParams.builder()
                    .setAmount(amountInCents)
                    .setCurrency(request.getCurrency().toLowerCase())
                    .setCustomer(stripeCustomerId)
                    .setDescription(request.getDescription() != null ? request.getDescription() : "Payment for order " + request.getOrderId())
                    .putAllMetadata(metadata)
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                    .setEnabled(true)
                                    .build()
                    );

            // Setup future usage if save payment method is true
            if (Boolean.TRUE.equals(request.getSavePaymentMethod())) {
                paramsBuilder.setSetupFutureUsage(PaymentIntentCreateParams.SetupFutureUsage.OFF_SESSION);
            }

            // Create payment intent with Stripe
            PaymentIntent paymentIntent = PaymentIntent.create(paramsBuilder.build());

            // Save payment record to database
            Payment payment = new Payment();
            payment.setOrderId(request.getOrderId());
            payment.setCustomerId(customerId);
            payment.setVendorOrganizationId(order.getVendorOrganizationId());
            payment.setStripePaymentIntentId(paymentIntent.getId());
            payment.setStripeCustomerId(stripeCustomerId);
            payment.setAmount((double) amountInCents);
            payment.setCurrency(request.getCurrency().toUpperCase());
            payment.setStatus(paymentIntent.getStatus());
            payment.setDescription(request.getDescription());
            payment.setCreatedAt(Instant.now());
            payment.setUpdatedAt(Instant.now());

            paymentRepository.save(payment);

            log.info("Payment intent created successfully: {}", paymentIntent.getId());

            return PaymentIntentResponseDto.builder()
                    .paymentIntentId(paymentIntent.getId())
                    .clientSecret(paymentIntent.getClientSecret())
                    .status(paymentIntent.getStatus())
                    .amount(request.getAmount())
                    .currency(request.getCurrency())
                    .orderId(request.getOrderId())
                    .customerId(customerId)
                    .message("Payment intent created successfully")
                    .publishableKey(stripeConfig.getPublishableKey())
                    .build();

        } catch (StripeException e) {
            log.error("Stripe error while creating payment intent: {}", e.getMessage(), e);
            throw new PaymentException("Failed to create payment intent: " + e.getMessage(), "STRIPE_API_ERROR", e);
        } catch (Exception e) {
            log.error("Error creating payment intent: {}", e.getMessage(), e);
            throw new PaymentException("Failed to create payment intent: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public PaymentResponseDto confirmPayment(String paymentIntentId) {
        try {
            log.info("Confirming payment for payment intent: {}", paymentIntentId);

            // Retrieve payment intent from Stripe
            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);

            // Find payment in database
            Payment payment = paymentRepository.findByStripePaymentIntentId(paymentIntentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Payment not found for payment intent: " + paymentIntentId));

            // Update payment status
            payment.setStatus(paymentIntent.getStatus());
            payment.setUpdatedAt(Instant.now());

            if ("succeeded".equals(paymentIntent.getStatus())) {
                payment.setPaidAt(Instant.now());

                // Get payment method details
                if (paymentIntent.getPaymentMethod() != null) {
                    PaymentMethod paymentMethod = PaymentMethod.retrieve(paymentIntent.getPaymentMethod());
                    payment.setPaymentMethod(paymentMethod.getType());

                    if (paymentMethod.getCard() != null) {
                        payment.setPaymentMethodType(paymentMethod.getCard().getBrand());
                        payment.setLast4(paymentMethod.getCard().getLast4());
                        payment.setBrand(paymentMethod.getCard().getBrand());
                    }
                }

                // Get charge details
                if (paymentIntent.getCharges() != null &&
                    paymentIntent.getCharges().getData() != null &&
                    !paymentIntent.getCharges().getData().isEmpty()) {
                    Charge charge = paymentIntent.getCharges().getData().get(0);
                    payment.setStripeChargeId(charge.getId());
                    payment.setReceiptUrl(charge.getReceiptUrl());
                }

                // Update order status
                Order order = orderRepository.findById(payment.getOrderId())
                        .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
                order.setStatus("confirmed");
                order.setUpdatedAt(Instant.now().toString());
                orderRepository.save(order);

                log.info("Payment confirmed successfully for order: {}", payment.getOrderId());
            } else if ("requires_payment_method".equals(paymentIntent.getStatus())) {
                payment.setFailureReason("Payment failed - requires new payment method");
            }

            paymentRepository.save(payment);

            return toPaymentResponseDto(payment);

        } catch (StripeException e) {
            log.error("Stripe error while confirming payment: {}", e.getMessage(), e);
            throw new PaymentException("Failed to confirm payment: " + e.getMessage(), "STRIPE_API_ERROR", e);
        } catch (Exception e) {
            log.error("Error confirming payment: {}", e.getMessage(), e);
            throw new PaymentException("Failed to confirm payment: " + e.getMessage(), e);
        }
    }

    @Override
    public PaymentResponseDto getPaymentById(String paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + paymentId));
        return toPaymentResponseDto(payment);
    }

    @Override
    public PaymentResponseDto getPaymentByPaymentIntentId(String paymentIntentId) {
        Payment payment = paymentRepository.findByStripePaymentIntentId(paymentIntentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for payment intent: " + paymentIntentId));
        return toPaymentResponseDto(payment);
    }

    @Override
    public List<PaymentResponseDto> getPaymentsByOrderId(String orderId) {
        return paymentRepository.findByOrderId(orderId).stream()
                .map(this::toPaymentResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PaymentResponseDto> getPaymentsByCustomerId(String customerId) {
        return paymentRepository.findByCustomerId(customerId).stream()
                .map(this::toPaymentResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PaymentResponseDto> getPaymentsByVendorId(String vendorOrganizationId) {
        return paymentRepository.findByVendorOrganizationId(vendorOrganizationId).stream()
                .map(this::toPaymentResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PaymentResponseDto cancelPayment(String paymentId) {
        try {
            log.info("Cancelling payment: {}", paymentId);

            Payment payment = paymentRepository.findById(paymentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + paymentId));

            // Only allow cancellation if payment is not already completed
            if ("succeeded".equals(payment.getStatus())) {
                throw new PaymentException("Cannot cancel a completed payment. Use refund instead.", "PAYMENT_ALREADY_COMPLETED");
            }

            // Cancel payment intent in Stripe
            PaymentIntent paymentIntent = PaymentIntent.retrieve(payment.getStripePaymentIntentId());
            paymentIntent.cancel();

            // Update payment status
            payment.setStatus("canceled");
            payment.setUpdatedAt(Instant.now());
            paymentRepository.save(payment);

            log.info("Payment cancelled successfully: {}", paymentId);

            return toPaymentResponseDto(payment);

        } catch (StripeException e) {
            log.error("Stripe error while cancelling payment: {}", e.getMessage(), e);
            throw new PaymentException("Failed to cancel payment: " + e.getMessage(), "STRIPE_API_ERROR", e);
        } catch (Exception e) {
            log.error("Error cancelling payment: {}", e.getMessage(), e);
            throw new PaymentException("Failed to cancel payment: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public PaymentResponseDto refundPayment(RefundRequestDto request) {
        try {
            log.info("Processing refund for payment: {}", request.getPaymentId());

            Payment payment = paymentRepository.findById(request.getPaymentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + request.getPaymentId()));

            // Validate payment is in succeeded state
            if (!"succeeded".equals(payment.getStatus())) {
                throw new PaymentException("Can only refund succeeded payments", "INVALID_PAYMENT_STATUS");
            }

            // Check if already refunded
            if ("refunded".equals(payment.getStatus())) {
                throw new PaymentException("Payment already refunded", "PAYMENT_ALREADY_REFUNDED");
            }

            // Create refund parameters
            RefundCreateParams.Builder refundParamsBuilder = RefundCreateParams.builder()
                    .setPaymentIntent(payment.getStripePaymentIntentId());

            // Set amount for partial refund
            if (request.getAmount() != null) {
                long refundAmountInCents = (long) (request.getAmount() * 100);
                if (refundAmountInCents > payment.getAmount().longValue()) {
                    throw new PaymentException("Refund amount cannot exceed payment amount", "INVALID_REFUND_AMOUNT");
                }
                refundParamsBuilder.setAmount(refundAmountInCents);
            }

            // Set reason
            if (request.getReason() != null) {
                refundParamsBuilder.setReason(RefundCreateParams.Reason.valueOf(request.getReason().toUpperCase()));
            }

            // Add metadata
            if (request.getDescription() != null) {
                refundParamsBuilder.putMetadata("description", request.getDescription());
            }

            // Create refund in Stripe
            Refund refund = Refund.create(refundParamsBuilder.build());

            // Update payment record
            payment.setRefundId(refund.getId());
            payment.setRefundAmount((double) refund.getAmount());
            payment.setRefundReason(request.getDescription());
            payment.setStatus(refund.getAmount().equals(payment.getAmount().longValue()) ? "refunded" : "partially_refunded");
            payment.setUpdatedAt(Instant.now());
            paymentRepository.save(payment);

            // Update order status if fully refunded
            if ("refunded".equals(payment.getStatus())) {
                Order order = orderRepository.findById(payment.getOrderId())
                        .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
                order.setStatus("cancelled");
                order.setUpdatedAt(Instant.now().toString());
                orderRepository.save(order);
            }

            log.info("Refund processed successfully for payment: {}", request.getPaymentId());

            return toPaymentResponseDto(payment);

        } catch (StripeException e) {
            log.error("Stripe error while processing refund: {}", e.getMessage(), e);
            throw new PaymentException("Failed to process refund: " + e.getMessage(), "STRIPE_API_ERROR", e);
        } catch (Exception e) {
            log.error("Error processing refund: {}", e.getMessage(), e);
            throw new PaymentException("Failed to process refund: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void handleWebhookEvent(String payload, String signatureHeader) {
        try {
            log.info("Processing webhook event");

            Event event;

            // Verify webhook signature if secret is configured
            if (stripeConfig.getWebhookSecret() != null && !stripeConfig.getWebhookSecret().isEmpty()) {
                try {
                    event = Webhook.constructEvent(payload, signatureHeader, stripeConfig.getWebhookSecret());
                } catch (SignatureVerificationException e) {
                    log.error("Webhook signature verification failed: {}", e.getMessage());
                    throw new PaymentException("Invalid webhook signature", "INVALID_SIGNATURE", e);
                }
            } else {
                // Parse event without verification (not recommended for production)
                // WARNING: This should only be used for testing
                log.warn("Webhook processed without signature verification - NOT SECURE FOR PRODUCTION");
                return; // Skip webhook processing if signature cannot be verified
            }

            log.info("Webhook event type: {}", event.getType());

            // Handle different event types
            switch (event.getType()) {
                case "payment_intent.succeeded":
                    handlePaymentIntentSucceeded(event);
                    break;
                case "payment_intent.payment_failed":
                    handlePaymentIntentFailed(event);
                    break;
                case "payment_intent.canceled":
                    handlePaymentIntentCanceled(event);
                    break;
                case "charge.refunded":
                    handleChargeRefunded(event);
                    break;
                case "charge.dispute.created":
                    handleDisputeCreated(event);
                    break;
                default:
                    log.info("Unhandled event type: {}", event.getType());
            }

        } catch (Exception e) {
            log.error("Error processing webhook event: {}", e.getMessage(), e);
            throw new PaymentException("Failed to process webhook event: " + e.getMessage(), e);
        }
    }

    @Override
    public String getOrCreateStripeCustomer(String customerId, String email, String name) {
        try {
            // Check if user already has a Stripe customer ID
            User user = userRepository.findById(customerId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + customerId));

            // If user has stripe customer ID, return it
            if (user.getStripeCustomerId() != null && !user.getStripeCustomerId().isEmpty()) {
                return user.getStripeCustomerId();
            }

            // Create new Stripe customer
            CustomerCreateParams params = CustomerCreateParams.builder()
                    .setEmail(email)
                    .setName(name)
                    .putMetadata("userId", customerId)
                    .build();

            Customer customer = Customer.create(params);

            // Save Stripe customer ID to user
            user.setStripeCustomerId(customer.getId());
            userRepository.save(user);

            log.info("Created Stripe customer: {} for user: {}", customer.getId(), customerId);

            return customer.getId();

        } catch (StripeException e) {
            log.error("Stripe error while creating customer: {}", e.getMessage(), e);
            throw new PaymentException("Failed to create Stripe customer: " + e.getMessage(), "STRIPE_API_ERROR", e);
        }
    }

    // Private helper methods

    private void handlePaymentIntentSucceeded(Event event) {
        PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer().getObject().orElse(null);
        if (paymentIntent != null) {
            log.info("Payment intent succeeded: {}", paymentIntent.getId());
            confirmPayment(paymentIntent.getId());
        }
    }

    private void handlePaymentIntentFailed(Event event) {
        PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer().getObject().orElse(null);
        if (paymentIntent != null) {
            log.info("Payment intent failed: {}", paymentIntent.getId());

            paymentRepository.findByStripePaymentIntentId(paymentIntent.getId())
                    .ifPresent(payment -> {
                        payment.setStatus("failed");
                        payment.setFailureReason(paymentIntent.getLastPaymentError() != null
                                ? paymentIntent.getLastPaymentError().getMessage()
                                : "Payment failed");
                        payment.setUpdatedAt(Instant.now());
                        paymentRepository.save(payment);
                    });
        }
    }

    private void handlePaymentIntentCanceled(Event event) {
        PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer().getObject().orElse(null);
        if (paymentIntent != null) {
            log.info("Payment intent canceled: {}", paymentIntent.getId());

            paymentRepository.findByStripePaymentIntentId(paymentIntent.getId())
                    .ifPresent(payment -> {
                        payment.setStatus("canceled");
                        payment.setUpdatedAt(Instant.now());
                        paymentRepository.save(payment);
                    });
        }
    }

    private void handleChargeRefunded(Event event) {
        Charge charge = (Charge) event.getDataObjectDeserializer().getObject().orElse(null);
        if (charge != null && charge.getPaymentIntent() != null) {
            log.info("Charge refunded: {}", charge.getId());

            paymentRepository.findByStripePaymentIntentId(charge.getPaymentIntent())
                    .ifPresent(payment -> {
                        payment.setStatus(charge.getRefunded() ? "refunded" : "partially_refunded");
                        payment.setRefundAmount((double) charge.getAmountRefunded());
                        payment.setUpdatedAt(Instant.now());
                        paymentRepository.save(payment);
                    });
        }
    }

    private void handleDisputeCreated(Event event) {
        Dispute dispute = (Dispute) event.getDataObjectDeserializer().getObject().orElse(null);
        if (dispute != null && dispute.getPaymentIntent() != null) {
            log.warn("Dispute created for payment intent: {}", dispute.getPaymentIntent());

            paymentRepository.findByStripePaymentIntentId(dispute.getPaymentIntent())
                    .ifPresent(payment -> {
                        payment.setStatus("disputed");
                        payment.setFailureReason("Payment disputed: " + dispute.getReason());
                        payment.setUpdatedAt(Instant.now());
                        paymentRepository.save(payment);
                    });
        }
    }

    private PaymentResponseDto toPaymentResponseDto(Payment payment) {
        return PaymentResponseDto.builder()
                .id(payment.getId())
                .orderId(payment.getOrderId())
                .customerId(payment.getCustomerId())
                .vendorOrganizationId(payment.getVendorOrganizationId())
                .stripePaymentIntentId(payment.getStripePaymentIntentId())
                .amount(payment.getAmount() != null ? payment.getAmount() / 100.0 : null)
                .currency(payment.getCurrency())
                .status(payment.getStatus())
                .paymentMethod(payment.getPaymentMethod())
                .paymentMethodType(payment.getPaymentMethodType())
                .last4(payment.getLast4())
                .brand(payment.getBrand())
                .description(payment.getDescription())
                .receiptUrl(payment.getReceiptUrl())
                .failureReason(payment.getFailureReason())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .paidAt(payment.getPaidAt())
                .build();
    }
}

