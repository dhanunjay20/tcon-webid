package com.tcon.webid.service;

import com.tcon.webid.dto.*;
import com.tcon.webid.entity.Vendor;
import com.tcon.webid.exception.ResourceNotFoundException;
import com.tcon.webid.repository.VendorRepository;
import com.tcon.webid.util.ContactUtils;
import com.tcon.webid.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class VendorServiceImpl implements VendorService {

    private static final Logger log = LoggerFactory.getLogger(VendorServiceImpl.class);

    @Autowired
    private VendorRepository vendorRepo;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private MailService mailService;
    @Autowired
    private EmailTemplateService emailTemplateService;

    @Override
    public VendorResponseDto registerVendor(VendorRegistrationDto dto) {
        // Ensure vendorOrganizationId must be provided by frontend — do not auto-generate on backend
        String orgId = dto.getVendorOrganizationId() == null ? null : dto.getVendorOrganizationId().trim();
        if (orgId == null || orgId.isBlank()) {
            throw new RuntimeException("vendorOrganizationId must be provided by the client");
        }

        String email = ContactUtils.normalizeEmail(dto.getEmail());
        String mobile = ContactUtils.normalizeMobile(dto.getMobile());

        if (vendorRepo.existsByEmail(email))
            throw new RuntimeException("Email already registered");
        if (vendorRepo.existsByMobile(mobile))
            throw new RuntimeException("Mobile number already registered");
        if (vendorRepo.existsByVendorOrganizationId(orgId))
            throw new RuntimeException("Organization ID already registered");

        Vendor vendor = new Vendor();
        vendor.setVendorOrganizationId(orgId);
        vendor.setBusinessName(dto.getBusinessName());
        vendor.setContactName(dto.getContactName());
        vendor.setEmail(email);
        vendor.setMobile(mobile);
        vendor.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        vendor.setAddresses(dto.getAddresses());
        vendor.setLicenseDocuments(dto.getLicenseDocuments());
        Vendor saved = vendorRepo.save(vendor);

        // Send vendorOrganizationId to vendor's email
        try {
            String emailSubject = "Vendor Registration Successful - Event Bidding";
            String htmlBody = emailTemplateService.generateVendorRegistrationEmail(
                saved.getContactName(),
                saved.getVendorOrganizationId(),
                saved.getBusinessName(),
                saved.getEmail(),
                saved.getMobile()
            );

            log.info("Sending vendorOrganizationId to vendor email: {}", saved.getEmail());
            mailService.sendHtmlMail(saved.getEmail(), emailSubject, htmlBody);
            log.info("VendorOrganizationId sent successfully to: {}", saved.getEmail());
        } catch (Exception e) {
            log.error("Failed to send vendorOrganizationId email to: {}", saved.getEmail(), e);
            // Don't fail registration if email fails, just log the error
        }

        return toResponseDto(saved);
    }

    @Override
    public VendorResponseDto loginVendor(String login, String password) {
        // normalize login for matching
        String normLogin = login == null ? null : (login.contains("@") ? ContactUtils.normalizeEmail(login) : ContactUtils.normalizeMobile(login));

        // Try to find vendor by vendorOrganizationId first, then email, then mobile (including legacy formats)
        Vendor vendor = vendorRepo.findByVendorOrganizationId(normLogin)
                .or(() -> vendorRepo.findByEmail(normLogin))
                .or(() -> vendorRepo.findByMobile(normLogin))
                .orElse(null);

        if (vendor == null && normLogin != null && !normLogin.contains("@")) {
            var candidates = ContactUtils.mobileSearchCandidates(normLogin);
            if (!candidates.isEmpty()) {
                var vendorOpt = vendorRepo.findByMobileIn(candidates);
                if (vendorOpt.isPresent()) vendor = vendorOpt.get();
            }
        }

        if (vendor == null) throw new RuntimeException("Account not found");

        if (!passwordEncoder.matches(password, vendor.getPasswordHash()))
            throw new RuntimeException("Invalid credentials");

        log.info("Vendor logged in successfully: {}", vendor.getVendorOrganizationId());
        return toResponseDto(vendor);
    }

    @Override
    public VendorResponseDto getVendorById(String id) {
        return vendorRepo.findById(id)
                .map(this::toResponseDto)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found"));
    }

    @Override
    public List<VendorResponseDto> getAllVendors() {
        return vendorRepo.findAll().stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public VendorResponseDto updateVendor(String id, VendorUpdateDto dto) {
        Vendor vendor = vendorRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found"));

        // Update fields if provided
        if (dto.getBusinessName() != null) vendor.setBusinessName(dto.getBusinessName());
        if (dto.getContactName() != null) vendor.setContactName(dto.getContactName());
        if (dto.getEmail() != null) vendor.setEmail(ContactUtils.normalizeEmail(dto.getEmail()));
        if (dto.getMobile() != null) vendor.setMobile(ContactUtils.normalizeMobile(dto.getMobile()));
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            vendor.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        }
        if (dto.getAddresses() != null) vendor.setAddresses(dto.getAddresses());
        if (dto.getLicenseDocuments() != null) vendor.setLicenseDocuments(dto.getLicenseDocuments());

        // Update new business information fields
        if (dto.getWebsite() != null && !dto.getWebsite().isBlank()) {
            vendor.setWebsite(dto.getWebsite());
            log.info("Updating vendor website to: {}", dto.getWebsite());
        }
        if (dto.getYearsInBusiness() != null) {
            vendor.setYearsInBusiness(dto.getYearsInBusiness());
            log.info("Updating vendor yearsInBusiness to: {}", dto.getYearsInBusiness());
        }
        if (dto.getAboutBusiness() != null && !dto.getAboutBusiness().isBlank()) {
            vendor.setAboutBusiness(dto.getAboutBusiness());
            log.info("Updating vendor aboutBusiness");
        }

        Vendor updated = vendorRepo.save(vendor);
        log.info("Vendor updated: {}", updated.getId());
        return toResponseDto(updated);
    }

    @Override
    public VendorResponseDto getVendorByOrganizationId(String organizationId) {
        return vendorRepo.findByVendorOrganizationId(organizationId)
                .map(this::toResponseDto)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found"));
    }

    @Override
    public VendorResponseDto mapVendorToDto(Vendor v) {
        return toResponseDto(v);
    }

    private VendorResponseDto toResponseDto(Vendor v) {
        return new VendorResponseDto(
                v.getId(),
                v.getVendorOrganizationId(),
                v.getBusinessName(),
                v.getContactName(),
                v.getEmail(),
                v.getMobile(),
                v.getAddresses(),
                v.getLicenseDocuments(),
                v.getIsOnline(),
                v.getLastSeenAt(),
                v.getWebsite(),
                v.getYearsInBusiness(),
                v.getAboutBusiness(),
                v.getLatitude(),
                v.getLongitude(),
                v.getLastLocationUpdatedAt(),
                null // distance is calculated dynamically by LocationService when needed
        );
    }
}
