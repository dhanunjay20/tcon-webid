package com.tcon.webid.service;

import com.twilio.Twilio;
import com.twilio.exception.ApiException;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class WhatsAppServiceImpl implements WhatsAppService {

    private static final Logger log = LoggerFactory.getLogger(WhatsAppServiceImpl.class);

    @Value("${twilio.accountSid:}")
    private String accountSid;

    @Value("${twilio.authToken:}")
    private String authToken;

    @Value("${twilio.apiKeySid:}")
    private String apiKeySid;

    @Value("${twilio.apiKeySecret:}")
    private String apiKeySecret;

    @Value("${twilio.whatsappFrom:}")
    private String whatsappFrom; // e.g. "whatsapp:+14155238886"

    private boolean initialized = false;

    @PostConstruct
    public void init() {
        try {
            // Trim values to avoid accidental whitespace
            if (accountSid != null) accountSid = accountSid.trim();
            if (authToken != null) authToken = authToken.trim();
            if (apiKeySid != null) apiKeySid = apiKeySid.trim();
            if (apiKeySecret != null) apiKeySecret = apiKeySecret.trim();
            if (whatsappFrom != null) whatsappFrom = whatsappFrom.trim();

            if (apiKeySid != null && !apiKeySid.isEmpty() && apiKeySecret != null && !apiKeySecret.isEmpty() && accountSid != null && !accountSid.isEmpty()) {
                if (whatsappFrom == null || whatsappFrom.trim().isEmpty()) {
                    log.error("Twilio configured (API Key) but twilio.whatsappFrom is not set; falling back to stub logging.");
                    initialized = false;
                    return;
                }
                // Initialize with API Key (recommended for rotation)
                Twilio.init(apiKeySid, apiKeySecret, accountSid);
                initialized = true;
                log.info("Twilio initialized with API Key successfully");
                return;
            }
            if (accountSid != null && !accountSid.isEmpty() && authToken != null && !authToken.isEmpty()) {
                if (whatsappFrom == null || whatsappFrom.trim().isEmpty()) {
                    log.error("Twilio configured (Account SID) but twilio.whatsappFrom is not set; falling back to stub logging.");
                    initialized = false;
                    return;
                }

                // Test credentials before marking as initialized
                log.info("========================================");
                log.info("Initializing Twilio with Account SID...");
                log.info("Account SID found: {}...", accountSid.substring(0, Math.min(10, accountSid.length())));
                log.info("Auth Token found: ***present***");
                log.info("WhatsApp From: {}", whatsappFrom);
                log.info("========================================");

                try {
                    boolean credentialsValid = com.tcon.webid.util.TwilioCredentialTester.testCredentials(accountSid, authToken);

                    if (credentialsValid) {
                        // Don't initialize again - testCredentials already did it
                        initialized = true;
                        log.info("========================================");
                        log.info("✓ Twilio initialization SUCCESSFUL");
                        log.info("✓ WhatsApp messages will be sent via Twilio");
                        log.info("========================================");
                    } else {
                        log.error("========================================");
                        log.error("✗ Twilio credentials test FAILED");
                        log.error("✗ WhatsApp will use STUB MODE (logging only)");
                        log.error("✗ Check logs above for detailed error information");
                        log.error("========================================");
                        initialized = false;
                    }
                } catch (Exception e) {
                    log.error("========================================");
                    log.error("✗ Twilio initialization threw exception: {}", e.getMessage(), e);
                    log.error("✗ Falling back to stub mode");
                    log.error("========================================");
                    initialized = false;
                }
            } else {
                log.warn("========================================");
                log.warn("Twilio not configured - no credentials found");
                log.warn("Looking for: twilio.accountSid and twilio.authToken");
                log.warn("WhatsApp will use STUB MODE (logging only)");
                log.warn("========================================");
                initialized = false;
            }
        } catch (Exception e) {
            log.error("Twilio init failed: {}", e.getMessage(), e);
            initialized = false;
        }
    }

    @Override
    public void sendWhatsAppMessage(String mobile, String message) {
        log.info("[WhatsApp] Attempting to send message to: {}", mobile);

        if (!initialized) {
            // Twilio not configured — fallback to logging for dev/test
            log.warn("=== WHATSAPP STUB MODE (Twilio Not Configured or Init Failed) ===");
            log.warn("[WhatsApp-STUB] To: {}", mobile);
            log.warn("[WhatsApp-STUB] Message: {}", message);
            log.warn("[WhatsApp-STUB] Message logged successfully (Twilio credentials not configured or initialization failed)");
            log.warn("=========================================================================");
            return;
        }

        // Format the 'to' number - ensure it has whatsapp: prefix and proper country code
        String cleaned = mobile.trim();
        // remove common separators
        // note: backslashes must be escaped in Java strings
        cleaned = cleaned.replaceAll("[\\\\s\\-()]+", "");

        String to;
        if (cleaned.startsWith("whatsapp:")) {
            to = cleaned;
        } else if (cleaned.startsWith("+")) {
            to = "whatsapp:" + cleaned;
        } else if (cleaned.matches("^[0-9]{10,15}$")) {
            // assume it's a national number without plus; add + and assume India if 10 digits
            if (cleaned.length() == 10) {
                to = "whatsapp:+91" + cleaned;
            } else {
                to = "whatsapp:+" + cleaned;
            }
        } else if (cleaned.startsWith("91") && cleaned.length() >= 12) {
            to = "whatsapp:+" + cleaned;
        } else {
            // last resort: prefix with + if starts with digits
            if (cleaned.matches("^[0-9]+$")) to = "whatsapp:+" + cleaned;
            else {
                log.error("Cannot normalize mobile number for WhatsApp: {}", mobile);
                return;
            }
        }

        String from = whatsappFrom.startsWith("whatsapp:") ? whatsappFrom : ("whatsapp:" + whatsappFrom);

        try {
            log.info("[WhatsApp] Sending via Twilio from: {} to: {}", from, to);
            log.info("[WhatsApp] Message content: {}", message);
            Message twilioMessage = Message.creator(
                    new PhoneNumber(to),
                    new PhoneNumber(from),
                    message
            ).create();
            log.info("[WhatsApp] Message sent successfully. SID: {}", twilioMessage.getSid());
            log.info("[WhatsApp] Message status: {}", twilioMessage.getStatus());
        } catch (ApiException e) {
            log.error("Twilio WhatsApp send failed: {}", e.getMessage());
            log.error("Twilio error code: {}", e.getCode());
            log.error("Twilio error details: {}", e.getMoreInfo());
            // Helpful hint for authentication errors
            if (e.getCode() != null && e.getCode() == 20003) {
                log.error("Twilio authentication failed (20003). Check Account SID / Auth Token or API Key credentials and ensure the credentials are for the correct Twilio project. See: https://www.twilio.com/docs/errors/20003");
            }
            log.error("Full stack trace:", e);
            // Fail soft: do NOT rethrow, so higher-level flows continue
            log.warn("WhatsApp send failed but flow will continue without raising an error to the caller.");
        } catch (Exception e) {
            log.error("Unexpected error sending WhatsApp message: {}", e.getMessage());
            log.error("Full stack trace:", e);
            // Also fail soft here
            log.warn("Unexpected WhatsApp error; continuing without propagating exception.");
        }
    }
}
