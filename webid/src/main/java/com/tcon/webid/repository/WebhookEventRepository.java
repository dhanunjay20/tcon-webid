package com.tcon.webid.repository;

import com.tcon.webid.entity.WebhookEvent;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for WebhookEvent entity
 * Used for tracking processed Stripe webhook events to ensure idempotency
 */
@Repository
public interface WebhookEventRepository extends MongoRepository<WebhookEvent, String> {

    /**
     * Find a webhook event by Stripe event ID
     * @param stripeEventId The Stripe event ID (evt_xxx)
     * @return Optional containing the webhook event if found
     */
    Optional<WebhookEvent> findByStripeEventId(String stripeEventId);

    /**
     * Check if a webhook event has been processed
     * @param stripeEventId The Stripe event ID
     * @return true if event exists (has been processed)
     */
    boolean existsByStripeEventId(String stripeEventId);
}

