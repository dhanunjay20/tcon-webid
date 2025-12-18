package com.tcon.webid.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * WebhookEvent entity for tracking processed Stripe webhook events
 * This ensures idempotency - prevents duplicate processing of the same webhook event
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "webhook_events")
public class WebhookEvent {

    @Id
    private String id;

    /**
     * Unique Stripe event ID (evt_xxx)
     * Indexed as unique to prevent duplicate processing
     */
    @Indexed(unique = true)
    private String stripeEventId;

    /**
     * Event type (e.g., payment_intent.succeeded)
     */
    private String eventType;

    /**
     * Processing status (processed, failed)
     */
    private String status;

    /**
     * When the event was received and processed
     */
    private Instant processedAt;

    /**
     * Optional error message if processing failed
     */
    private String errorMessage;

    /**
     * Timestamp from Stripe event
     */
    private Instant eventCreatedAt;
}

