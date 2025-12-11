package com.tcon.webid.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import com.tcon.webid.util.ContactUtils;

@Service
public class OtpServiceImpl implements OtpService {

    private static final Logger log = LoggerFactory.getLogger(OtpServiceImpl.class);

    private static class OtpEntry {
        String otp;
        Instant expiresAt;
    }

    private final Map<String, OtpEntry> store = new ConcurrentHashMap<>();
    private final Random random = new Random();

    @Autowired
    private MailService mailService;
    @Autowired
    private WhatsAppService whatsAppService;
    @Autowired
    private EmailTemplateService emailTemplateService;

    @Override
    public void generateAndSendOtp(String contact) {
        // ...existing code...
        // Normalize contact so OTP keys are consistent
        String normContact = contact != null && contact.contains("@") ? ContactUtils.normalizeEmail(contact) : ContactUtils.normalizeMobile(contact);
        String otp = String.format("%06d", random.nextInt(1_000_000));
        OtpEntry entry = new OtpEntry();
        entry.otp = otp;
        entry.expiresAt = Instant.now().plusSeconds(5 * 60); // 5 minutes
        store.put(normContact, entry);

        log.info("Generated OTP for contact: {} (normalized: {})", contact, normContact);
        log.info("OTP value (FOR DEBUGGING ONLY - REMOVE IN PRODUCTION): {}", otp);

        // Send via email or WhatsApp depending on contact format
        if (normContact.contains("@")) {
            log.info("Sending OTP via email to: {}", normContact);
            String htmlBody = emailTemplateService.generateOtpEmail(otp);
            mailService.sendHtmlMail(normContact, "Your OTP - Event Bidding", htmlBody);
            log.info("OTP email sent successfully to: {}", normContact);
        } else {
            log.info("Sending OTP via WhatsApp to: {}", normContact);
            whatsAppService.sendWhatsAppMessage(normContact, "Your OTP for Event Bidding is: " + otp + ". This OTP will expire in 5 minutes.");
            log.info("OTP WhatsApp message sent successfully to: {}", normContact);
        }
    }

    @Override
    public boolean verifyOtp(String contact, String otp) {
        String normContact = contact != null && contact.contains("@") ? ContactUtils.normalizeEmail(contact) : ContactUtils.normalizeMobile(contact);
        log.info("Verifying OTP for contact: {} (normalized: {})", contact, normContact);
        OtpEntry entry = store.get(normContact);
        if (entry == null) {
            log.warn("No OTP found for contact: {}", normContact);
            return false;
        }
        if (Instant.now().isAfter(entry.expiresAt)) {
            log.warn("OTP expired for contact: {}", normContact);
            store.remove(normContact);
            return false;
        }
        boolean ok = entry.otp.equals(otp);
        if (ok) {
            log.info("OTP verified successfully for contact: {}", normContact);
            store.remove(normContact);
        } else {
            log.warn("Invalid OTP provided for contact: {}", normContact);
        }
        return ok;
    }

    @Override
    public void removeOtp(String contact) {
        String normContact = contact != null && contact.contains("@") ? ContactUtils.normalizeEmail(contact) : ContactUtils.normalizeMobile(contact);
        log.info("Removing OTP for contact: {} (normalized: {})", contact, normContact);
        store.remove(normContact);
    }
}
