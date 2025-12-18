package com.tcon.webid.config;

import com.stripe.Stripe;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class StripeConfig {

    // Allow empty default so app can start without keys configured
    @Value("${stripe.api.key:}")
    private String stripeApiKey;

    @Value("${stripe.webhook.secret:}")
    private String webhookSecret;

    @Value("${stripe.publishable.key:}")
    private String publishableKey;

    @PostConstruct
    public void init() {
        // Validate API key is present when required
        if (stripeApiKey == null || stripeApiKey.trim().isEmpty() || stripeApiKey.contains("your_test_secret_key")) {
            log.warn("Stripe API key not configured. Set 'stripe.api.key' in application.properties for Stripe integration.");
        } else {
            // Initialize Stripe SDK with API key
            Stripe.apiKey = stripeApiKey;
            log.info("Stripe API initialized");
        }

        // Log webhook secret status
        if (webhookSecret == null || webhookSecret.trim().isEmpty()) {
            log.warn("Stripe webhook secret not configured - webhook signature verification will be SKIPPED");
            log.warn("This is acceptable for development but NOT SECURE for production");
        } else {
            log.info("Stripe webhook signature verification: ENABLED");
        }

        // Log publishable key status
        if (publishableKey != null && !publishableKey.isEmpty() && !publishableKey.contains("your_test_publishable_key")) {
            log.info("Stripe publishable key is configured");
        } else {
            log.warn("Stripe publishable key is not configured - required for client-side integration");
        }

        log.info("Stripe configuration complete");
    }

    public String getWebhookSecret() {
        return webhookSecret;
    }

    public String getPublishableKey() {
        return publishableKey;
    }

    public boolean isProduction() {
        return false; // Simplified - no profile logic
    }
}
