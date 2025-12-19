package com.tcon.webid.service;

import com.tcon.webid.dto.admin.*;
import com.tcon.webid.entity.*;
import com.tcon.webid.repository.*;
import com.tcon.webid.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final AdminRepository adminRepository;
    private final UserRepository userRepository;
    private final VendorRepository vendorRepository;
    private final OrderRepository orderRepository;
    private final BidRepository bidRepository;
    private final MenuItemRepository menuItemRepository;
    private final PaymentRepository paymentRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    // ==================== Admin Authentication ====================

    @Override
    public AdminResponseDto registerAdmin(AdminRegistrationDto dto) {
        log.info("Registering new admin: {}", dto.getUsername());

        if (adminRepository.existsByUsername(dto.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        if (adminRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        Admin admin = new Admin();
        admin.setUsername(dto.getUsername());
        admin.setEmail(dto.getEmail());
        admin.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        admin.setFirstName(dto.getFirstName());
        admin.setLastName(dto.getLastName());
        admin.setRole(dto.getRole() != null ? dto.getRole() : "ADMIN");
        admin.setCreatedAt(LocalDateTime.now().format(DATE_FORMATTER));
        admin.setActive(true);

        Admin saved = adminRepository.save(admin);
        log.info("Admin registered successfully: {}", saved.getUsername());

        String token = jwtUtil.generateToken(saved.getUsername(), "ADMIN");

        return toAdminResponseDto(saved, token);
    }

    @Override
    public AdminResponseDto loginAdmin(String username, String password) {
        log.info("Admin login attempt: {}", username);

        Admin admin = adminRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!admin.isActive()) {
            throw new RuntimeException("Admin account is inactive");
        }

        if (!passwordEncoder.matches(password, admin.getPasswordHash())) {
            throw new RuntimeException("Invalid credentials");
        }

        admin.setLastLoginAt(LocalDateTime.now().format(DATE_FORMATTER));
        adminRepository.save(admin);

        String token = jwtUtil.generateToken(admin.getUsername(), "ADMIN");

        log.info("Admin logged in successfully: {}", username);
        return toAdminResponseDto(admin, token);
    }

    @Override
    public AdminResponseDto getAdminById(String id) {
        Admin admin = adminRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Admin not found"));
        return toAdminResponseDto(admin, null);
    }

    @Override
    public List<AdminResponseDto> getAllAdmins() {
        log.info("Fetching all admins");
        return adminRepository.findAll().stream()
                .map(admin -> toAdminResponseDto(admin, null))
                .collect(Collectors.toList());
    }

    // ==================== Dashboard Stats ====================

    @Override
    public AdminDashboardStatsDto getDashboardStats() {
        log.info("Fetching admin dashboard stats");

        long totalUsers = userRepository.count();
        long totalVendors = vendorRepository.count();
        long totalOrders = orderRepository.count();
        long totalBids = bidRepository.count();
        long totalMenuItems = menuItemRepository.count();
        long totalPayments = paymentRepository.count();

        double totalRevenue = paymentRepository.findAll().stream()
                .filter(p -> "succeeded".equals(p.getStatus()))
                .mapToDouble(p -> {
                    if (p.getAmountInCents() != null) {
                        return p.getAmountInCents() / 100.0;
                    } else if (p.getAmount() != null) {
                        return p.getAmount() / 100.0;
                    }
                    return 0.0;
                })
                .sum();

        long pendingOrders = orderRepository.findAll().stream()
                .filter(o -> "pending".equals(o.getStatus()))
                .count();

        long completedOrders = orderRepository.findAll().stream()
                .filter(o -> "completed".equals(o.getStatus()))
                .count();

        long activeVendors = vendorRepository.findAll().stream()
                .filter(v -> Boolean.TRUE.equals(v.getIsOnline()))
                .count();

        return AdminDashboardStatsDto.builder()
                .totalUsers(totalUsers)
                .totalVendors(totalVendors)
                .totalOrders(totalOrders)
                .totalBids(totalBids)
                .totalMenuItems(totalMenuItems)
                .totalPayments(totalPayments)
                .totalRevenue(totalRevenue)
                .pendingOrders(pendingOrders)
                .completedOrders(completedOrders)
                .activeVendors(activeVendors)
                .build();
    }

    // ==================== User Management ====================

    @Override
    public List<AdminUserDto> getAllUsers() {
        log.info("Fetching all users");
        return userRepository.findAll().stream()
                .map(this::toAdminUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public AdminUserDto getUserById(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return toAdminUserDto(user);
    }

    @Override
    public void deleteUser(String id) {
        log.info("Deleting user: {}", id);
        userRepository.deleteById(id);
    }

    // ==================== Vendor Management ====================

    @Override
    public List<AdminVendorDto> getAllVendors() {
        log.info("Fetching all vendors");
        return vendorRepository.findAll().stream()
                .map(this::toAdminVendorDto)
                .collect(Collectors.toList());
    }

    @Override
    public AdminVendorDto getVendorById(String id) {
        Vendor vendor = vendorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vendor not found"));
        return toAdminVendorDto(vendor);
    }

    @Override
    public void deleteVendor(String id) {
        log.info("Deleting vendor: {}", id);
        vendorRepository.deleteById(id);
    }

    @Override
    public AdminVendorDto updateVendorStatus(String id, Boolean isActive) {
        log.info("Updating vendor status: {} - {}", id, isActive);
        Vendor vendor = vendorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vendor not found"));
        vendor.setIsOnline(isActive);
        Vendor updated = vendorRepository.save(vendor);
        return toAdminVendorDto(updated);
    }

    // ==================== Order Management ====================

    @Override
    public List<AdminOrderDto> getAllOrders() {
        log.info("Fetching all orders");
        return orderRepository.findAll().stream()
                .map(this::toAdminOrderDto)
                .collect(Collectors.toList());
    }

    @Override
    public AdminOrderDto getOrderById(String id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        return toAdminOrderDto(order);
    }

    @Override
    public void deleteOrder(String id) {
        log.info("Deleting order: {}", id);
        orderRepository.deleteById(id);
    }

    @Override
    public AdminOrderDto updateOrderStatus(String id, String status) {
        log.info("Updating order status: {} - {}", id, status);
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setStatus(status);
        order.setUpdatedAt(LocalDateTime.now().format(DATE_FORMATTER));
        Order updated = orderRepository.save(order);
        return toAdminOrderDto(updated);
    }

    // ==================== Bid Management ====================

    @Override
    public List<AdminBidDto> getAllBids() {
        log.info("Fetching all bids");
        return bidRepository.findAll().stream()
                .map(this::toAdminBidDto)
                .collect(Collectors.toList());
    }

    @Override
    public AdminBidDto getBidById(String id) {
        Bid bid = bidRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bid not found"));
        return toAdminBidDto(bid);
    }

    @Override
    public void deleteBid(String id) {
        log.info("Deleting bid: {}", id);
        bidRepository.deleteById(id);
    }

    // ==================== Menu Item Management ====================

    @Override
    public List<AdminMenuItemDto> getAllMenuItems() {
        log.info("Fetching all menu items");
        return menuItemRepository.findAll().stream()
                .map(this::toAdminMenuItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public AdminMenuItemDto getMenuItemById(String id) {
        MenuItem menuItem = menuItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Menu item not found"));
        return toAdminMenuItemDto(menuItem);
    }

    @Override
    public void deleteMenuItem(String id) {
        log.info("Deleting menu item: {}", id);
        menuItemRepository.deleteById(id);
    }

    // ==================== Payment Management ====================

    @Override
    public List<AdminPaymentDto> getAllPayments() {
        log.info("Fetching all payments");
        return paymentRepository.findAll().stream()
                .map(this::toAdminPaymentDto)
                .collect(Collectors.toList());
    }

    @Override
    public AdminPaymentDto getPaymentById(String id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        return toAdminPaymentDto(payment);
    }

    // ==================== Helper Methods ====================

    private AdminResponseDto toAdminResponseDto(Admin admin, String token) {
        return AdminResponseDto.builder()
                .id(admin.getId())
                .username(admin.getUsername())
                .email(admin.getEmail())
                .firstName(admin.getFirstName())
                .lastName(admin.getLastName())
                .role(admin.getRole())
                .createdAt(admin.getCreatedAt())
                .lastLoginAt(admin.getLastLoginAt())
                .isActive(admin.isActive())
                .token(token)
                .build();
    }

    private AdminUserDto toAdminUserDto(User user) {
        return AdminUserDto.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .mobile(user.getMobile())
                .addresses(user.getAddresses())
                .profileUrl(user.getProfileUrl())
                .latitude(user.getLatitude())
                .longitude(user.getLongitude())
                .lastLocationUpdatedAt(user.getLastLocationUpdatedAt())
                .stripeCustomerId(user.getStripeCustomerId())
                .build();
    }

    private AdminVendorDto toAdminVendorDto(Vendor vendor) {
        return AdminVendorDto.builder()
                .id(vendor.getId())
                .vendorOrganizationId(vendor.getVendorOrganizationId())
                .businessName(vendor.getBusinessName())
                .contactName(vendor.getContactName())
                .email(vendor.getEmail())
                .mobile(vendor.getMobile())
                .addresses(vendor.getAddresses())
                .licenseDocuments(vendor.getLicenseDocuments())
                .isOnline(vendor.getIsOnline())
                .lastSeenAt(vendor.getLastSeenAt())
                .website(vendor.getWebsite())
                .yearsInBusiness(vendor.getYearsInBusiness())
                .aboutBusiness(vendor.getAboutBusiness())
                .latitude(vendor.getLatitude())
                .longitude(vendor.getLongitude())
                .lastLocationUpdatedAt(vendor.getLastLocationUpdatedAt())
                .build();
    }

    private AdminOrderDto toAdminOrderDto(Order order) {
        String customerName = "Unknown";
        if (order.getCustomerId() != null) {
            User user = userRepository.findById(order.getCustomerId()).orElse(null);
            if (user != null) {
                customerName = (user.getFirstName() != null ? user.getFirstName() + " " : "") +
                              (user.getLastName() != null ? user.getLastName() : "");
                customerName = customerName.trim();
                if (customerName.isEmpty()) {
                    customerName = user.getEmail();
                }
            }
        }

        String vendorName = "Unknown";
        if (order.getVendorOrganizationId() != null) {
            Vendor vendor = vendorRepository.findByVendorOrganizationId(order.getVendorOrganizationId()).orElse(null);
            if (vendor != null) {
                vendorName = vendor.getBusinessName();
            }
        }

        return AdminOrderDto.builder()
                .id(order.getId())
                .customerId(order.getCustomerId())
                .vendorOrganizationId(order.getVendorOrganizationId())
                .eventName(order.getEventName())
                .eventDate(order.getEventDate())
                .eventLocation(order.getEventLocation())
                .guestCount(order.getGuestCount())
                .menuItems(order.getMenuItems())
                .status(order.getStatus())
                .totalPrice(order.getTotalPrice())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .customerName(customerName)
                .vendorName(vendorName)
                .build();
    }

    private AdminBidDto toAdminBidDto(Bid bid) {
        return AdminBidDto.builder()
                .id(bid.getId())
                .orderId(bid.getOrderId())
                .vendorOrganizationId(bid.getVendorOrganizationId())
                .proposedMessage(bid.getProposedMessage())
                .proposedTotalPrice(bid.getProposedTotalPrice())
                .status(bid.getStatus())
                .submittedAt(bid.getSubmittedAt())
                .updatedAt(bid.getUpdatedAt())
                .customerName(bid.getCustomerName())
                .vendorBusinessName(bid.getVendorBusinessName())
                .eventName(bid.getEventName())
                .build();
    }

    private AdminMenuItemDto toAdminMenuItemDto(MenuItem menuItem) {
        String vendorName = "Unknown";
        if (menuItem.getVendorOrganizationId() != null) {
            Vendor vendor = vendorRepository.findByVendorOrganizationId(menuItem.getVendorOrganizationId()).orElse(null);
            if (vendor != null) {
                vendorName = vendor.getBusinessName();
            }
        }

        return AdminMenuItemDto.builder()
                .id(menuItem.getId())
                .vendorOrganizationId(menuItem.getVendorOrganizationId())
                .name(menuItem.getName())
                .description(menuItem.getDescription())
                .images(menuItem.getImages())
                .category(menuItem.getCategory())
                .subCategory(menuItem.getSubCategory())
                .ingredients(menuItem.getIngredients())
                .spiceLevels(menuItem.getSpiceLevels())
                .available(menuItem.isAvailable())
                .vendorName(vendorName)
                .build();
    }

    private AdminPaymentDto toAdminPaymentDto(Payment payment) {
        String customerName = "Unknown";
        if (payment.getCustomerId() != null) {
            User user = userRepository.findById(payment.getCustomerId()).orElse(null);
            if (user != null) {
                customerName = (user.getFirstName() != null ? user.getFirstName() + " " : "") +
                              (user.getLastName() != null ? user.getLastName() : "");
                customerName = customerName.trim();
                if (customerName.isEmpty()) {
                    customerName = user.getEmail();
                }
            }
        }

        String vendorName = "Unknown";
        if (payment.getVendorOrganizationId() != null) {
            Vendor vendor = vendorRepository.findByVendorOrganizationId(payment.getVendorOrganizationId()).orElse(null);
            if (vendor != null) {
                vendorName = vendor.getBusinessName();
            }
        }

        return AdminPaymentDto.builder()
                .id(payment.getId())
                .orderId(payment.getOrderId())
                .customerId(payment.getCustomerId())
                .vendorOrganizationId(payment.getVendorOrganizationId())
                .stripePaymentIntentId(payment.getStripePaymentIntentId())
                .stripeCustomerId(payment.getStripeCustomerId())
                .stripeChargeId(payment.getStripeChargeId())
                .amountInCents(payment.getAmountInCents())
                .currency(payment.getCurrency())
                .status(payment.getStatus())
                .paidAt(payment.getPaidAt())
                .createdAt(payment.getCreatedAt())
                .customerName(customerName)
                .vendorName(vendorName)
                .build();
    }
}

