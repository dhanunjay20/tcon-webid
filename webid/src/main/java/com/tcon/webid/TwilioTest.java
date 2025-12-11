package com.tcon.webid;

import com.tcon.webid.util.TwilioCredentialTester;

import java.io.InputStream;
import java.util.Properties;

/**
 * Standalone Twilio credential test
 * Run this to verify your Twilio credentials work
 *
 * To run:
 * cd C:\Users\dhanu\GitHub\tcon-webid\webid
 * mvn exec:java -Dexec.mainClass="com.tcon.webid.TwilioTest"
 */
public class TwilioTest {

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("Twilio Credential Test");
        System.out.println("========================================");
        System.out.println();

        Properties props = new Properties();
        try (InputStream in = TwilioTest.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (in != null) props.load(in);
        } catch (Exception e) {
            System.err.println("Failed to load application.properties from classpath: " + e.getMessage());
        }

        String accountSid = props.getProperty("twilio.accountSid");
        String authToken = props.getProperty("twilio.authToken");
        String whatsappFrom = props.getProperty("twilio.whatsappFrom");

        System.out.println("Properties loaded from application.properties:");
        System.out.println("  twilio.accountSid: " + (accountSid != null ? accountSid.substring(0, Math.min(10, accountSid.length())) + "..." : "NOT FOUND"));
        System.out.println("  twilio.authToken: " + (authToken != null ? "***present*** (" + authToken.length() + " chars)" : "NOT FOUND"));
        System.out.println("  twilio.whatsappFrom: " + (whatsappFrom != null ? whatsappFrom : "NOT FOUND"));
        System.out.println();

        if (accountSid == null || accountSid.isEmpty()) {
            System.err.println("ERROR: twilio.accountSid not found in application.properties");
            System.exit(1);
        }

        if (authToken == null || authToken.isEmpty()) {
            System.err.println("ERROR: twilio.authToken not found in application.properties");
            System.exit(1);
        }

        // Test credentials
        boolean valid = TwilioCredentialTester.testCredentials(accountSid, authToken);

        System.out.println();
        System.out.println("========================================");
        if (valid) {
            System.out.println("✓✓✓ RESULT: Credentials are VALID ✓✓✓");
            System.out.println("Your Twilio account is working correctly!");
            System.out.println("WhatsApp messages will be sent when you run the app.");
        } else {
            System.out.println("✗✗✗ RESULT: Credentials are INVALID ✗✗✗");
            System.out.println("Please follow the instructions above to fix.");
        }
        System.out.println("========================================");

        System.exit(valid ? 0 : 1);
    }
}
