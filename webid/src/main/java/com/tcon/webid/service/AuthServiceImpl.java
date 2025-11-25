package com.tcon.webid.service;

import com.tcon.webid.dto.LoginRequestDto;
import com.tcon.webid.dto.AuthResponseDto;
import com.tcon.webid.entity.User;
import com.tcon.webid.entity.Vendor;
import com.tcon.webid.repository.UserRepository;
import com.tcon.webid.repository.VendorRepository;
import com.tcon.webid.util.JwtUtil;
import com.tcon.webid.util.ContactUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    @Autowired
    private UserRepository userRepo;
    @Autowired
    private VendorRepository vendorRepo;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private MailService mailService;
    @Autowired
    private WhatsAppService whatsAppService;

    @Override
    public AuthResponseDto login(LoginRequestDto loginDto) {
        String login = loginDto.getLogin() != null ? loginDto.getLogin().trim() : "";

        // If login looks like an email, normalize it for case-insensitive match
        if (login.contains("@")) {
            String normEmail = ContactUtils.normalizeEmail(login);
            User user = userRepo.findByEmail(normEmail).orElse(null);
            if (user != null && passwordEncoder.matches(loginDto.getPassword(), user.getPasswordHash())) {
                String jwt = jwtUtil.generateToken(user.getEmail(), "USER");
                log.info("User logged in successfully: {}", user.getEmail());
                return AuthResponseDto.ofUser(user, jwt);
            }

            Vendor vendor = vendorRepo.findByEmail(normEmail).orElse(null);
            if (vendor != null && passwordEncoder.matches(loginDto.getPassword(), vendor.getPasswordHash())) {
                String jwt = jwtUtil.generateToken(vendor.getVendorOrganizationId(), "VENDOR");
                log.info("Vendor logged in successfully: {}", vendor.getVendorOrganizationId());
                return AuthResponseDto.ofVendor(vendor, jwt);
            }

            log.warn("Invalid login attempt for email: {}", normEmail);
            throw new RuntimeException("Invalid login credentials");
        }

        // Otherwise, try mobile or vendorOrganizationId or raw matches
        // Try exact user by email/mobile first
        User user = userRepo.findByEmail(login).orElse(
                userRepo.findByMobile(login).orElse(null));
        if (user != null && passwordEncoder.matches(loginDto.getPassword(), user.getPasswordHash())) {
            String jwt = jwtUtil.generateToken(user.getEmail(), "USER");
            log.info("User logged in successfully: {}", user.getEmail());
            return AuthResponseDto.ofUser(user, jwt);
        }

        // Try vendor exact matches
        Vendor vendor = vendorRepo.findByVendorOrganizationId(login)
                .or(() -> vendorRepo.findByEmail(login))
                .or(() -> vendorRepo.findByMobile(login))
                .orElse(null);
        if (vendor != null && passwordEncoder.matches(loginDto.getPassword(), vendor.getPasswordHash())) {
            String jwt = jwtUtil.generateToken(vendor.getVendorOrganizationId(), "VENDOR");
            log.info("Vendor logged in successfully: {}", vendor.getVendorOrganizationId());
            return AuthResponseDto.ofVendor(vendor, jwt);
        }

        // Try normalized mobile variants (e.g., user entered 9848299232 -> DB stored +919848299232 etc.)
        String normMobile = ContactUtils.normalizeMobile(login);
        if (normMobile != null && !normMobile.isBlank()) {
            user = userRepo.findByMobile(normMobile).orElse(null);
            if (user == null) {
                var candidates = ContactUtils.mobileSearchCandidates(normMobile);
                if (!candidates.isEmpty()) user = userRepo.findByMobileIn(candidates).orElse(null);
            }
            if (user != null && passwordEncoder.matches(loginDto.getPassword(), user.getPasswordHash())) {
                String jwt = jwtUtil.generateToken(user.getEmail(), "USER");
                log.info("User logged in successfully (mobile variant): {}", user.getEmail());
                return AuthResponseDto.ofUser(user, jwt);
            }

            vendor = vendorRepo.findByMobile(normMobile).orElse(null);
            if (vendor == null) {
                var candidates = ContactUtils.mobileSearchCandidates(normMobile);
                if (!candidates.isEmpty()) vendor = vendorRepo.findByMobileIn(candidates).orElse(null);
            }
            if (vendor != null && passwordEncoder.matches(loginDto.getPassword(), vendor.getPasswordHash())) {
                String jwt = jwtUtil.generateToken(vendor.getVendorOrganizationId(), "VENDOR");
                log.info("Vendor logged in successfully (mobile variant): {}", vendor.getVendorOrganizationId());
                return AuthResponseDto.ofVendor(vendor, jwt);
            }
        }

        log.warn("Invalid login attempt for: {}", login);
        throw new RuntimeException("Invalid login credentials");
    }

    @Override
    public void sendUsernameToUser(String contact) {
        if (contact == null || contact.isBlank()) {
            log.error("sendUsernameToUser called with null or blank contact");
            throw new RuntimeException("Contact information is required");
        }

        // Normalize input for consistent lookups
        String normalized = contact.trim();
        boolean isEmail = normalized.contains("@");
        if (isEmail) normalized = ContactUtils.normalizeEmail(normalized);
        else normalized = ContactUtils.normalizeMobile(normalized);

        try {
            User user = null;
            Vendor vendor = null;

            // Check if contact is email or mobile
            if (isEmail) {
                log.debug("Looking up user by email: {}", normalized);
                user = userRepo.findByEmail(normalized).orElse(null);
                if (user == null) {
                    log.debug("No user found with email, checking vendor: {}", normalized);
                    vendor = vendorRepo.findByEmail(normalized).orElse(null);
                }
            } else {
                log.debug("Looking up user by mobile: {}", normalized);
                // Try exact normalized first
                user = userRepo.findByMobile(normalized).orElse(null);
                if (user == null) {
                    // Try legacy/alternate formats (e.g., '9848299232' stored in DB)
                    var candidates = ContactUtils.mobileSearchCandidates(normalized);
                    if (!candidates.isEmpty()) {
                        var userOpt = userRepo.findByMobileIn(candidates);
                        if (userOpt.isPresent()) user = userOpt.get();
                    }
                }
                if (user == null) {
                    log.debug("No user found with mobile, checking vendor: {}", normalized);
                    vendor = vendorRepo.findByMobile(normalized).orElse(null);
                    if (vendor == null) {
                        var candidates = ContactUtils.mobileSearchCandidates(normalized);
                        if (!candidates.isEmpty()) {
                            var vendorOpt = vendorRepo.findByMobileIn(candidates);
                            if (vendorOpt.isPresent()) vendor = vendorOpt.get();
                        }
                    }
                }
            }

            if (user != null) {
                String username = user.getEmail(); // Using email as username
                String message = "Your username is: " + username;

                if (isEmail) {
                    log.info("Sending username to email: {}", normalized);
                    try {
                        mailService.sendSimpleMail(normalized, "Your Username - Event Bidding", message);
                        log.info("Username sent successfully via email to: {}", normalized);
                    } catch (Exception e) {
                        log.error("Failed to send username email to: {}", normalized, e);
                        // Do not rethrow; email is best-effort
                    }
                } else {
                    log.info("Sending username to WhatsApp: {} (raw input: {})", normalized, contact);
                    try {
                        whatsAppService.sendWhatsAppMessage(normalized, message);
                        log.info("Username sent successfully via WhatsApp to: {}", normalized);
                    } catch (Exception e) {
                        log.error("Failed to send username WhatsApp to: {}", normalized, e);
                        // Do not rethrow; WhatsApp is best-effort
                    }
                }
            } else if (vendor != null) {
                String username = vendor.getVendorOrganizationId(); // Using vendorOrganizationId as username
                String message = String.format(
                    "Your Vendor Organization ID is: %s\n" +
                    "Please use this ID to login to the system.\n\n" +
                    "Business Name: %s",
                    username,
                    vendor.getBusinessName()
                );

                if (isEmail) {
                    log.info("Sending vendor username to email: {}", normalized);
                    try {
                        mailService.sendSimpleMail(normalized, "Your Vendor Organization ID - Event Bidding", message);
                        log.info("Vendor username sent successfully via email to: {}", normalized);
                    } catch (Exception e) {
                        log.error("Failed to send vendor username email to: {}", normalized, e);
                        // Do not rethrow; email is best-effort
                    }
                } else {
                    log.info("Sending vendor username to WhatsApp: {} (raw input: {})", normalized, contact);
                    try {
                        whatsAppService.sendWhatsAppMessage(normalized, message);
                        log.info("Vendor username sent successfully via WhatsApp to: {}", normalized);
                    } catch (Exception e) {
                        log.error("Failed to send vendor username WhatsApp to: {}", normalized, e);
                        // Do not rethrow; WhatsApp is best-effort
                    }
                }
            } else {
                log.warn("No user or vendor found for contact: {} (normalized input: {})", contact, normalized);
                // For security, do not throw or reveal existence
            }
        } catch (Exception e) {
            // Catch any unexpected errors so controllers still return generic success
            log.error("Unexpected error in sendUsernameToUser for contact={}", contact, e);
        }
    }
}
