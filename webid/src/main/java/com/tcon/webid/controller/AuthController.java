package com.tcon.webid.controller;

import com.tcon.webid.dto.LoginRequestDto;
import com.tcon.webid.dto.AuthResponseDto;
import com.tcon.webid.service.AuthService;
import com.tcon.webid.dto.ForgotPasswordDto;
import com.tcon.webid.dto.ForgotUsernameDto;
import com.tcon.webid.dto.VerifyOtpDto;
import com.tcon.webid.dto.ResetPasswordDto;
import com.tcon.webid.entity.User;
import com.tcon.webid.entity.Vendor;
import com.tcon.webid.repository.UserRepository;
import com.tcon.webid.repository.VendorRepository;
import com.tcon.webid.service.OtpService;
import com.tcon.webid.service.UserService;
import com.tcon.webid.service.VendorService;
import com.tcon.webid.util.ContactUtils;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthService authService;

    @Autowired
    private OtpService otpService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VendorRepository vendorRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private VendorService vendorService;

    @PostMapping("/login")
    public AuthResponseDto login(@RequestBody @Valid LoginRequestDto dto) {
        return authService.login(dto);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody ForgotPasswordDto dto) {
        String email = (dto != null) ? dto.getEmail() : null;
        String mobile = (dto != null) ? dto.getMobile() : null;

        try {
            // Normalize inputs using canonical utilities
            final String normEmail = ContactUtils.normalizeEmail(email);
            final String normMobile = ContactUtils.normalizeMobile(mobile);

            boolean accountFound = false;

            if (normEmail != null) {
                // Check User by email
                var userOpt = userRepository.findByEmail(normEmail);
                if (userOpt.isPresent()) {
                    otpService.generateAndSendOtp(normEmail);
                    accountFound = true;
                    log.info("OTP sent to User email: {}", normEmail);
                } else {
                    // Check Vendor by email
                    var vendorOpt = vendorRepository.findByEmail(normEmail);
                    if (vendorOpt.isPresent()) {
                        otpService.generateAndSendOtp(normEmail);
                        accountFound = true;
                        log.info("OTP sent to Vendor email: {}", normEmail);
                    }
                }
            } else if (normMobile != null) {
                // Check User by mobile
                var userOpt = userRepository.findByMobile(normMobile);
                if (userOpt.isPresent()) {
                    otpService.generateAndSendOtp(normMobile);
                    accountFound = true;
                    log.info("OTP sent to User mobile: {}", normMobile);
                } else {
                    // Try mobile search candidates for User
                    List<String> candidates = ContactUtils.mobileSearchCandidates(normMobile);
                    if (!candidates.isEmpty()) {
                        var userCandidateOpt = userRepository.findByMobileIn(candidates);
                        if (userCandidateOpt.isPresent()) {
                            otpService.generateAndSendOtp(normMobile);
                            accountFound = true;
                            log.info("OTP sent to User mobile (via candidates): {}", normMobile);
                        }
                    }

                    // Check Vendor by mobile if not found in User
                    if (!accountFound) {
                        var vendorOpt = vendorRepository.findByMobile(normMobile);
                        if (vendorOpt.isPresent()) {
                            otpService.generateAndSendOtp(normMobile);
                            accountFound = true;
                            log.info("OTP sent to Vendor mobile: {}", normMobile);
                        } else {
                            // Try mobile search candidates for Vendor
                            if (!candidates.isEmpty()) {
                                var vendorCandidateOpt = vendorRepository.findByMobileIn(candidates);
                                if (vendorCandidateOpt.isPresent()) {
                                    otpService.generateAndSendOtp(normMobile);
                                    accountFound = true;
                                    log.info("OTP sent to Vendor mobile (via candidates): {}", normMobile);
                                }
                            }
                        }
                    }
                }
            }

            if (!accountFound) {
                log.info("No User or Vendor found with email={}, mobile={}", normEmail, normMobile);
            }

            // Always return generic response for security
            return ResponseEntity.ok("If an account exists for the provided contact, an OTP has been sent to reset the password.");
        } catch (Exception e) {
            log.error("Error in forgotPassword for email={} mobile={}", email, mobile, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unable to process request");
        }
    }

    /**
     * User-only forgot password endpoint (checks User repository only)
     */
    @PostMapping("/forgot-password/user")
    public ResponseEntity<String> forgotPasswordForUser(@RequestBody ForgotPasswordDto dto) {
        String email = (dto != null) ? dto.getEmail() : null;
        String mobile = (dto != null) ? dto.getMobile() : null;

        try {
            final String normEmail = ContactUtils.normalizeEmail(email);
            final String normMobile = ContactUtils.normalizeMobile(mobile);

            boolean sent = false;

            if (normEmail != null) {
                var userOpt = userRepository.findByEmail(normEmail);
                if (userOpt.isPresent()) {
                    otpService.generateAndSendOtp(normEmail);
                    sent = true;
                    log.info("OTP sent to User email (user-only endpoint): {}", normEmail);
                }
            }

            if (!sent && normMobile != null) {
                var userOpt = userRepository.findByMobile(normMobile);
                if (userOpt.isPresent()) {
                    otpService.generateAndSendOtp(normMobile);
                    sent = true;
                    log.info("OTP sent to User mobile (user-only endpoint): {}", normMobile);
                } else {
                    var candidates = ContactUtils.mobileSearchCandidates(normMobile);
                    if (!candidates.isEmpty()) {
                        var userCandidateOpt = userRepository.findByMobileIn(candidates);
                        if (userCandidateOpt.isPresent()) {
                            otpService.generateAndSendOtp(normMobile);
                            sent = true;
                            log.info("OTP sent to User mobile (via candidates) (user-only): {}", normMobile);
                        }
                    }
                }
            }

            // Always return generic message
            return ResponseEntity.ok("If a user account exists for the provided contact, an OTP has been sent to reset the password.");
        } catch (Exception e) {
            log.error("Error in forgotPasswordForUser for email={} mobile={}", email, mobile, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unable to process request");
        }
    }

    @PostMapping("/forgot-username")
    public ResponseEntity<String> forgotUsername(@RequestBody ForgotUsernameDto dto) {
        String email = (dto != null) ? dto.getEmail() : null;
        String mobile = (dto != null) ? dto.getMobile() : null;

        try {
            log.info("Forgot username request received for email={}, mobile={}", email, mobile);

            // Validate that at least one field is provided
            if ((email == null || email.isBlank()) && (mobile == null || mobile.isBlank())) {
                log.warn("Forgot username request with no email or mobile");
                return ResponseEntity.badRequest().body("Email or mobile number is required");
            }

            // Normalize inputs to canonical forms
            final String normEmail = ContactUtils.normalizeEmail(email);
            final String normMobile = ContactUtils.normalizeMobile(mobile);

            boolean accountFound = false;
            List<String> candidates = null;

            // Check email first
            if (normEmail != null) {
                // Check User by email
                var userOpt = userRepository.findByEmail(normEmail);
                if (userOpt.isPresent()) {
                    log.info("User found with email: {}, sending username", normEmail);
                    authService.sendUsernameToUser(normEmail);
                    accountFound = true;
                } else {
                    // Check Vendor by email
                    var vendorOpt = vendorRepository.findByEmail(normEmail);
                    if (vendorOpt.isPresent()) {
                        log.info("Vendor found with email: {}, sending username", normEmail);
                        authService.sendUsernameToUser(normEmail);
                        accountFound = true;
                    } else {
                        log.info("No User or Vendor found with email: {}", normEmail);
                    }
                }
            }

            // Check mobile if not found by email
            if (!accountFound && normMobile != null) {
                // Check User by mobile
                var userOpt = userRepository.findByMobile(normMobile);
                if (userOpt.isPresent()) {
                    log.info("User found with mobile: {}, sending username", normMobile);
                    authService.sendUsernameToUser(normMobile);
                    accountFound = true;
                } else {
                    // Try mobile search candidates for User
                    candidates = ContactUtils.mobileSearchCandidates(normMobile);
                    log.info("Trying mobile candidates for User: {}", candidates);
                    if (!candidates.isEmpty()) {
                        var userCandidateOpt = userRepository.findByMobileIn(candidates);
                        if (userCandidateOpt.isPresent()) {
                            log.info("User found with mobile candidates: {}, sending username", normMobile);
                            authService.sendUsernameToUser(normMobile);
                            accountFound = true;
                        }
                    }

                    // Check Vendor by mobile if not found in User
                    if (!accountFound) {
                        var vendorOpt = vendorRepository.findByMobile(normMobile);
                        if (vendorOpt.isPresent()) {
                            log.info("Vendor found with mobile: {}, sending username", normMobile);
                            authService.sendUsernameToUser(normMobile);
                            accountFound = true;
                        } else {
                            // Try mobile search candidates for Vendor
                            log.info("Trying mobile candidates for Vendor: {}", candidates);
                            if (!candidates.isEmpty()) {
                                var vendorCandidateOpt = vendorRepository.findByMobileIn(candidates);
                                if (vendorCandidateOpt.isPresent()) {
                                    log.info("Vendor found with mobile candidates: {}, sending username", normMobile);
                                    authService.sendUsernameToUser(normMobile);
                                    accountFound = true;
                                }
                            }
                        }
                    }

                    if (!accountFound) {
                        log.info("No User or Vendor found with mobile: {} (candidates: {})", normMobile, candidates);
                    }
                }
            }

            // Always return generic response for security (don't reveal if user exists)
            return ResponseEntity.ok("If an account exists for the provided contact, your username has been sent via email or WhatsApp.");
        } catch (Exception e) {
            log.error("Error in forgotUsername for email={} mobile={}", email, mobile, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unable to process request");
        }
    }

    /**
     * User-only forgot username endpoint (checks User repository only)
     */
    @PostMapping("/forgot-username/user")
    public ResponseEntity<String> forgotUsernameForUser(@RequestBody ForgotUsernameDto dto) {
        String email = (dto != null) ? dto.getEmail() : null;
        String mobile = (dto != null) ? dto.getMobile() : null;

        try {
            log.info("User-only forgot username request received for email={}, mobile={}", email, mobile);

            if ((email == null || email.isBlank()) && (mobile == null || mobile.isBlank())) {
                log.warn("Forgot username (user-only) request with no email or mobile");
                return ResponseEntity.badRequest().body("Email or mobile number is required");
            }

            final String normEmail = ContactUtils.normalizeEmail(email);
            final String normMobile = ContactUtils.normalizeMobile(mobile);

            boolean sent = false;

            if (normEmail != null) {
                var userOpt = userRepository.findByEmail(normEmail);
                if (userOpt.isPresent()) {
                    authService.sendUsernameToUser(normEmail);
                    sent = true;
                    log.info("User username sent to email (user-only): {}", normEmail);
                }
            }

            if (!sent && normMobile != null) {
                var userOpt = userRepository.findByMobile(normMobile);
                if (userOpt.isPresent()) {
                    authService.sendUsernameToUser(normMobile);
                    sent = true;
                    log.info("User username sent to mobile (user-only): {}", normMobile);
                } else {
                    var candidates = ContactUtils.mobileSearchCandidates(normMobile);
                    if (!candidates.isEmpty()) {
                        var userCandidateOpt = userRepository.findByMobileIn(candidates);
                        if (userCandidateOpt.isPresent()) {
                            authService.sendUsernameToUser(normMobile);
                            sent = true;
                            log.info("User username sent to mobile (via candidates) (user-only): {}", normMobile);
                        }
                    }
                }
            }

            return ResponseEntity.ok("If a user account exists for the provided contact, your username has been sent via email or WhatsApp.");
        } catch (Exception e) {
            log.error("Error in forgotUsernameForUser for email={} mobile={}", email, mobile, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unable to process request");
        }
    }

    @PostMapping("/retrieve-username")
    public ResponseEntity<String> retrieveUsername(@RequestBody @Valid VerifyOtpDto dto) {
        try {
            log.info("Retrieve username request for contact: {}", dto.getContact());

            String normContact = dto.getContact() != null && dto.getContact().contains("@") ? ContactUtils.normalizeEmail(dto.getContact()) : ContactUtils.normalizeMobile(dto.getContact());

            boolean otpValid = otpService.verifyOtp(normContact, dto.getOtp());
            if (!otpValid) {
                log.warn("Invalid or expired OTP for contact: {}", dto.getContact());
                return ResponseEntity.badRequest().body("Invalid or expired OTP");
            }

            log.info("OTP verified successfully, sending username to: {}", normContact);
            authService.sendUsernameToUser(normContact);

            return ResponseEntity.ok("Your username has been sent to your registered contact.");
        } catch (Exception e) {
            log.error("Error in retrieveUsername for contact={}", dto.getContact(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unable to retrieve username");
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<String> verifyOtp(@RequestBody @Valid VerifyOtpDto dto) {
        String normContact = dto.getContact() != null && dto.getContact().contains("@") ? ContactUtils.normalizeEmail(dto.getContact()) : ContactUtils.normalizeMobile(dto.getContact());
        boolean ok = otpService.verifyOtp(normContact, dto.getOtp());
        if (!ok) return ResponseEntity.badRequest().body("Invalid or expired OTP");
        return ResponseEntity.ok("OTP verified");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody @Valid ResetPasswordDto dto) {
        String normContact = dto.getContact() != null && dto.getContact().contains("@") ? ContactUtils.normalizeEmail(dto.getContact()) : ContactUtils.normalizeMobile(dto.getContact());
        boolean ok = otpService.verifyOtp(normContact, dto.getOtp());
        if (!ok) return ResponseEntity.badRequest().body("Invalid or expired OTP");

        // Find user or vendor and update password
        User user = null;
        Vendor vendor = null;

        if (normContact != null && normContact.contains("@")) {
            // Check User by email
            user = userRepository.findByEmail(normContact).orElse(null);
            if (user == null) {
                // Check Vendor by email
                vendor = vendorRepository.findByEmail(normContact).orElse(null);
            }
        } else if (normContact != null) {
            // Check User by mobile
            user = userRepository.findByMobile(normContact).orElse(null);
            if (user == null) {
                // Try mobile search candidates for User
                List<String> candidates = ContactUtils.mobileSearchCandidates(normContact);
                if (!candidates.isEmpty()) {
                    user = userRepository.findByMobileIn(candidates).orElse(null);
                }
            }
            if (user == null) {
                // Check Vendor by mobile
                vendor = vendorRepository.findByMobile(normContact).orElse(null);
                if (vendor == null) {
                    // Try mobile search candidates for Vendor
                    List<String> candidates = ContactUtils.mobileSearchCandidates(normContact);
                    if (!candidates.isEmpty()) {
                        vendor = vendorRepository.findByMobileIn(candidates).orElse(null);
                    }
                }
            }
        }

        if (user == null && vendor == null) {
            return ResponseEntity.badRequest().body("User or Vendor not found");
        }

        // Update password for User or Vendor
        if (user != null) {
            com.tcon.webid.dto.UserUpdateDto upd = new com.tcon.webid.dto.UserUpdateDto();
            upd.setPassword(dto.getNewPassword());
            userService.updateUser(user.getId(), upd);
            log.info("Password reset successful for User: {}", user.getEmail());
        } else if (vendor != null) {
            com.tcon.webid.dto.VendorUpdateDto upd = new com.tcon.webid.dto.VendorUpdateDto();
            upd.setPassword(dto.getNewPassword());
            vendorService.updateVendor(vendor.getId(), upd);
            log.info("Password reset successful for Vendor: {}", vendor.getEmail());
        }

        return ResponseEntity.ok("Password reset successful");
    }
}
