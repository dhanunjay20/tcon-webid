package com.tcon.webid.config;

import com.stripe.Stripe;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class StripeConfig {

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    @Value("${stripe.webhook.secret:}")
    private String webhookSecret;

    @Value("${stripe.publishable.key:}")
    private String publishableKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeApiKey;
        log.info("Stripe API initialized successfully");

        if (webhookSecret != null && !webhookSecret.isEmpty()) {
            log.info("Stripe webhook secret configured");
        } else {
            log.warn("Stripe webhook secret not configured. Webhook signature verification will be skipped.");
        }

        if (publishableKey != null && !publishableKey.isEmpty()) {
            log.info("Stripe publishable key is configured");
        }
    }

    public String getWebhookSecret() {
        return webhookSecret;
    }

    public String getPublishableKey() {
        return publishableKey;
    }
}
