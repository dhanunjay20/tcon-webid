package com.tcon.webid.util;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.Account;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility to test Twilio credentials
 * This is a standalone class for debugging - not used in production flow
 */
public class TwilioCredentialTester {

    private static final Logger log = LoggerFactory.getLogger(TwilioCredentialTester.class);

    public static boolean testCredentials(String accountSid, String authToken) {
        try {
            log.info("Testing Twilio credentials...");
            log.info("Account SID: {}", accountSid != null ? accountSid.substring(0, Math.min(10, accountSid.length())) + "..." : "null");
            log.info("Auth Token: {}", authToken != null ? "***present***" : "null");

            if (accountSid == null || accountSid.isEmpty()) {
                log.error("Account SID is null or empty");
                return false;
            }

            if (authToken == null || authToken.isEmpty()) {
                log.error("Auth Token is null or empty");
                return false;
            }

            // Initialize Twilio (this is safe to call multiple times - it's idempotent)
            Twilio.init(accountSid, authToken);

            // Try to fetch account details (this will fail if credentials are wrong)
            log.info("Fetching account details from Twilio to verify credentials...");
            Account account = Account.fetcher(accountSid).fetch();

            log.info("========================================");
            log.info("✓ Twilio credentials are VALID");
            log.info("✓ Account Status: {}", account.getStatus());
            log.info("✓ Account Friendly Name: {}", account.getFriendlyName());
            log.info("========================================");

            return true;

        } catch (com.twilio.exception.ApiException e) {
            log.error("========================================");
            log.error("✗ Twilio credentials test FAILED");
            log.error("Error Code: {}", e.getCode());
            log.error("Error Message: {}", e.getMessage());
            log.error("More Info: {}", e.getMoreInfo());

            if (e.getCode() != null && e.getCode() == 20003) {
                log.error("");
                log.error("=== TWILIO AUTHENTICATION ERROR (20003) ===");
                log.error("This means your Account SID or Auth Token is incorrect.");
                log.error("");
                log.error("To fix this:");
                log.error("1. Go to https://console.twilio.com/");
                log.error("2. Log in to your account");
                log.error("3. Go to Account Info section on the dashboard");
                log.error("4. Copy the Account SID");
                log.error("5. Click 'View' on Auth Token and copy it");
                log.error("6. Update your application.properties (src/main/resources/application.properties) with the correct values:");
                log.error("   twilio.accountSid=AC...");
                log.error("   twilio.authToken=...");
                log.error("7. Restart the application");
                log.error("==========================================");
            }
            log.error("========================================");

            return false;

        } catch (Exception e) {
            log.error("========================================");
            log.error("✗ Unexpected error testing Twilio credentials: {}", e.getMessage(), e);
            log.error("========================================");
            return false;
        }
    }
}
