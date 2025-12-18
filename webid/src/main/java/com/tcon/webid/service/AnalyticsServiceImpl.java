package com.tcon.webid.service;

import com.tcon.webid.dto.analytics.*;
import com.tcon.webid.entity.Order;
import com.tcon.webid.entity.Payment;
import com.tcon.webid.entity.User;
import com.tcon.webid.entity.MenuItemReview;
import com.tcon.webid.repository.OrderRepository;
import com.tcon.webid.repository.PaymentRepository;
import com.tcon.webid.repository.UserRepository;
import com.tcon.webid.repository.MenuItemReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

/**
 * Service implementation for analytics and dashboard data
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final MenuItemReviewRepository reviewRepository;
    private final MongoTemplate mongoTemplate;

    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("MMM");

    @Override
    public List<MonthlyComparisonDto> getMonthlyComparison(String vendorOrganizationId) {
        log.info("Fetching monthly comparison data for vendorOrganizationId: {}", vendorOrganizationId);

        LocalDate now = LocalDate.now();
        int currentYear = now.getYear();
        int lastYear = currentYear - 1;

        // Get payments for this vendorOrganizationId
        List<Payment> allPayments = paymentRepository.findByVendorOrganizationId(vendorOrganizationId);
        log.debug("Found {} payments for vendorOrganizationId: {}", allPayments.size(), vendorOrganizationId);

        Map<String, Double> thisYearRevenue = new HashMap<>();
        Map<String, Double> lastYearRevenue = new HashMap<>();

        // Initialize all months to 0
        for (int month = 1; month <= 12; month++) {
            String monthKey = String.format("%02d", month);
            thisYearRevenue.put(monthKey, 0.0);
            lastYearRevenue.put(monthKey, 0.0);
        }

        // Aggregate revenue by month and year
        for (Payment payment : allPayments) {
            if (payment.getPaidAt() != null && "succeeded".equals(payment.getStatus())) {
                LocalDateTime paidDate = LocalDateTime.ofInstant(payment.getPaidAt(), ZoneId.systemDefault());
                int year = paidDate.getYear();
                String monthKey = String.format("%02d", paidDate.getMonthValue());

                double amount = payment.getAmountInCents() != null
                    ? payment.getAmountInCents() / 100.0
                    : (payment.getAmount() != null ? payment.getAmount() / 100.0 : 0.0);

                if (year == currentYear) {
                    thisYearRevenue.merge(monthKey, amount, Double::sum);
                } else if (year == lastYear) {
                    lastYearRevenue.merge(monthKey, amount, Double::sum);
                }
            }
        }

        // Build result list for the past 6 months
        List<MonthlyComparisonDto> result = new ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            LocalDate date = now.minusMonths(i);
            String monthName = date.format(MONTH_FORMATTER);
            String monthKey = String.format("%02d", date.getMonthValue());

            result.add(MonthlyComparisonDto.builder()
                .month(monthName)
                .thisYear(thisYearRevenue.getOrDefault(monthKey, 0.0))
                .lastYear(lastYearRevenue.getOrDefault(monthKey, 0.0))
                .build());
        }

        return result;
    }

    @Override
    public List<OrderVolumeDto> getOrderVolumeTrends(String vendorOrganizationId) {
        log.info("Fetching order volume trends for vendorOrganizationId: {}", vendorOrganizationId);

        LocalDate now = LocalDate.now();
        LocalDate sixWeeksAgo = now.minusWeeks(6);

        // Get all orders for the vendorOrganizationId
        List<Order> orders = orderRepository.findByVendorOrganizationId(vendorOrganizationId);
        log.debug("Found {} orders for vendorOrganizationId: {}", orders.size(), vendorOrganizationId);

        // Group orders by week
        Map<Integer, Integer> totalOrdersByWeek = new HashMap<>();
        Map<Integer, Integer> completedOrdersByWeek = new HashMap<>();

        WeekFields weekFields = WeekFields.of(Locale.getDefault());

        for (Order order : orders) {
            if (order.getCreatedAt() != null) {
                try {
                    LocalDate orderDate = LocalDate.parse(order.getCreatedAt().substring(0, 10));

                    if (orderDate.isAfter(sixWeeksAgo) || orderDate.isEqual(sixWeeksAgo)) {
                        int weekNumber = (int) ChronoUnit.WEEKS.between(sixWeeksAgo, orderDate);

                        if (weekNumber >= 0 && weekNumber < 6) {
                            totalOrdersByWeek.merge(weekNumber, 1, Integer::sum);

                            if ("completed".equals(order.getStatus())) {
                                completedOrdersByWeek.merge(weekNumber, 1, Integer::sum);
                            }
                        }
                    }
                } catch (Exception e) {
                    log.warn("Error parsing date for order {}: {}", order.getId(), e.getMessage());
                }
            }
        }

        // Build result list
        List<OrderVolumeDto> result = new ArrayList<>();
        for (int week = 0; week < 6; week++) {
            result.add(OrderVolumeDto.builder()
                .date("Week " + (week + 1))
                .orders(totalOrdersByWeek.getOrDefault(week, 0))
                .completed(completedOrdersByWeek.getOrDefault(week, 0))
                .build());
        }

        return result;
    }

    @Override
    public List<PopularMenuItemDto> getPopularMenuItems(String vendorOrganizationId, int limit) {
        log.info("Fetching popular menu items for vendorOrganizationId: {}", vendorOrganizationId);

        // Get all orders for the vendorOrganizationId
        List<Order> orders = orderRepository.findByVendorOrganizationId(vendorOrganizationId);
        log.debug("Found {} orders for vendorOrganizationId: {}", orders.size(), vendorOrganizationId);

        // Count menu item occurrences and calculate revenue
        Map<String, Integer> orderCounts = new HashMap<>();
        Map<String, Double> revenues = new HashMap<>();

        for (Order order : orders) {
            if (order.getMenuItems() != null) {
                for (Order.OrderMenuItem item : order.getMenuItems()) {
                    String itemName = item.getName();
                    orderCounts.merge(itemName, item.getQuantity(), Integer::sum);
                    revenues.merge(itemName, item.getPrice() * item.getQuantity(), Double::sum);
                }
            }
        }

        // Convert to list and sort by order count
        return orderCounts.entrySet().stream()
            .map(entry -> PopularMenuItemDto.builder()
                .item(entry.getKey())
                .orders(entry.getValue())
                .revenue(revenues.getOrDefault(entry.getKey(), 0.0))
                .build())
            .sorted(Comparator.comparingInt(PopularMenuItemDto::getOrders).reversed())
            .limit(limit)
            .collect(Collectors.toList());
    }

    @Override
    public List<RevenueTrendDto> getRevenueTrends(String vendorOrganizationId) {
        log.info("Fetching revenue trends for vendorOrganizationId: {}", vendorOrganizationId);

        LocalDate now = LocalDate.now();
        int currentYear = now.getYear();

        // Get payments for current year
        List<Payment> payments = paymentRepository.findByVendorOrganizationId(vendorOrganizationId);
        log.debug("Found {} payments for vendorOrganizationId: {}", payments.size(), vendorOrganizationId);

        Map<String, Double> revenueByMonth = new HashMap<>();

        // Initialize all months to 0
        for (int month = 1; month <= 12; month++) {
            String monthKey = String.format("%02d", month);
            revenueByMonth.put(monthKey, 0.0);
        }

        // Aggregate revenue by month
        for (Payment payment : payments) {
            if (payment.getPaidAt() != null && "succeeded".equals(payment.getStatus())) {
                LocalDateTime paidDate = LocalDateTime.ofInstant(payment.getPaidAt(), ZoneId.systemDefault());

                if (paidDate.getYear() == currentYear) {
                    String monthKey = String.format("%02d", paidDate.getMonthValue());

                    double amount = payment.getAmountInCents() != null
                        ? payment.getAmountInCents() / 100.0
                        : (payment.getAmount() != null ? payment.getAmount() / 100.0 : 0.0);

                    revenueByMonth.merge(monthKey, amount, Double::sum);
                }
            }
        }

        // Calculate average monthly revenue for target calculation
        double avgRevenue = revenueByMonth.values().stream()
            .mapToDouble(Double::doubleValue)
            .average()
            .orElse(0.0);

        // Build result list for the past 6 months
        List<RevenueTrendDto> result = new ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            LocalDate date = now.minusMonths(i);
            String monthName = date.format(MONTH_FORMATTER);
            String monthKey = String.format("%02d", date.getMonthValue());

            double actualRevenue = revenueByMonth.getOrDefault(monthKey, 0.0);
            // Set target as 90% of actual revenue or average, whichever is higher
            double target = Math.max(actualRevenue * 0.9, avgRevenue * 0.95);

            result.add(RevenueTrendDto.builder()
                .month(monthName)
                .revenue(actualRevenue)
                .target(target)
                .build());
        }

        return result;
    }

    @Override
    public List<OrderTableDto> getRecentOrders(String vendorOrganizationId, int limit) {
        log.info("Fetching recent orders for vendorOrganizationId: {}", vendorOrganizationId);

        List<Order> orders = orderRepository.findByVendorOrganizationId(vendorOrganizationId);
        log.debug("Found {} orders for vendorOrganizationId: {}", orders.size(), vendorOrganizationId);

        // Sort by created date descending and limit
        return orders.stream()
            .sorted((o1, o2) -> {
                String date1 = o1.getCreatedAt() != null ? o1.getCreatedAt() : "";
                String date2 = o2.getCreatedAt() != null ? o2.getCreatedAt() : "";
                return date2.compareTo(date1);
            })
            .limit(limit)
            .map(order -> {
                // Get customer name
                String clientName = "Unknown Client";
                if (order.getCustomerId() != null) {
                    userRepository.findById(order.getCustomerId())
                        .ifPresent(user -> {
                            String firstName = user.getFirstName() != null ? user.getFirstName() : "";
                            String lastName = user.getLastName() != null ? user.getLastName() : "";
                            // Update clientName in closure
                        });
                }

                // Try to get actual client name from user
                User customer = userRepository.findById(order.getCustomerId() != null ? order.getCustomerId() : "").orElse(null);
                if (customer != null) {
                    clientName = (customer.getFirstName() != null ? customer.getFirstName() : "") + " " +
                                (customer.getLastName() != null ? customer.getLastName() : "");
                    clientName = clientName.trim();
                    if (clientName.isEmpty()) {
                        clientName = customer.getEmail() != null ? customer.getEmail() : "Unknown Client";
                    }
                }

                return OrderTableDto.builder()
                    .id(order.getId())
                    .client(clientName)
                    .event(order.getEventName() != null ? order.getEventName() : "Event")
                    .date(order.getEventDate() != null ? order.getEventDate() : "TBD")
                    .guests(order.getGuestCount())
                    .status(order.getStatus() != null ? order.getStatus() : "pending")
                    .amount(String.format("$%.2f", order.getTotalPrice()))
                    .build();
            })
            .collect(Collectors.toList());
    }

    @Override
    public List<RecentActivityDto> getRecentActivities(String vendorOrganizationId, int limit) {
        log.info("Fetching recent activities for vendorOrganizationId: {}", vendorOrganizationId);

        List<RecentActivityDto> activities = new ArrayList<>();
        long activityId = 1;

        // Get recent orders
        List<Order> recentOrders = orderRepository.findByVendorOrganizationId(vendorOrganizationId)
            .stream()
            .sorted((o1, o2) -> {
                String date1 = o1.getCreatedAt() != null ? o1.getCreatedAt() : "";
                String date2 = o2.getCreatedAt() != null ? o2.getCreatedAt() : "";
                return date2.compareTo(date1);
            })
            .limit(5)
            .collect(Collectors.toList());

        for (Order order : recentOrders) {
            String userName = "Unknown User";
            if (order.getCustomerId() != null) {
                User customer = userRepository.findById(order.getCustomerId()).orElse(null);
                if (customer != null) {
                    userName = (customer.getFirstName() != null ? customer.getFirstName() : "") + " " +
                               (customer.getLastName() != null ? customer.getLastName() : "");
                    userName = userName.trim();
                    if (userName.isEmpty()) {
                        userName = customer.getEmail();
                    }
                }
            }

            String timeAgo = calculateTimeAgo(order.getCreatedAt());

            activities.add(RecentActivityDto.builder()
                .id(activityId++)
                .user(userName)
                .action("submitted a new order request")
                .time(timeAgo)
                .type("order")
                .build());
        }

        // Get recent reviews
        List<MenuItemReview> reviews = reviewRepository.findAll()
            .stream()
            .sorted((r1, r2) -> {
                String date1 = r1.getReviewDate() != null ? r1.getReviewDate() : "";
                String date2 = r2.getReviewDate() != null ? r2.getReviewDate() : "";
                return date2.compareTo(date1);
            })
            .limit(3)
            .collect(Collectors.toList());

        for (MenuItemReview review : reviews) {
            String userName = review.getCustomerName() != null ? review.getCustomerName() : "Anonymous";
            String timeAgo = calculateTimeAgo(review.getReviewDate());

            activities.add(RecentActivityDto.builder()
                .id(activityId++)
                .user(userName)
                .action(String.format("left a %d-star review", review.getStars()))
                .time(timeAgo)
                .type("review")
                .build());
        }

        // Get recent completed payments
        List<Payment> recentPayments = paymentRepository.findByVendorOrganizationId(vendorOrganizationId)
            .stream()
            .filter(p -> "succeeded".equals(p.getStatus()) && p.getPaidAt() != null)
            .sorted((p1, p2) -> {
                if (p1.getPaidAt() == null) return 1;
                if (p2.getPaidAt() == null) return -1;
                return p2.getPaidAt().compareTo(p1.getPaidAt());
            })
            .limit(2)
            .collect(Collectors.toList());

        for (Payment payment : recentPayments) {
            String userName = "Customer";
            if (payment.getCustomerId() != null) {
                User customer = userRepository.findById(payment.getCustomerId()).orElse(null);
                if (customer != null) {
                    userName = (customer.getFirstName() != null ? customer.getFirstName() : "") + " " +
                               (customer.getLastName() != null ? customer.getLastName() : "");
                    userName = userName.trim();
                    if (userName.isEmpty()) {
                        userName = customer.getEmail();
                    }
                }
            }

            String timeAgo = payment.getPaidAt() != null
                ? calculateTimeAgo(payment.getPaidAt().toString())
                : "recently";

            activities.add(RecentActivityDto.builder()
                .id(activityId++)
                .user(userName)
                .action("completed payment")
                .time(timeAgo)
                .type("bid")
                .build());
        }

        // Sort all activities by time and limit
        return activities.stream()
            .sorted(Comparator.comparingLong(RecentActivityDto::getId))
            .limit(limit)
            .collect(Collectors.toList());
    }

    @Override
    public DashboardDataDto getCompleteDashboardData(String vendorId) {
        log.info("Fetching complete dashboard data for vendor: {}", vendorId);

        return DashboardDataDto.builder()
            .monthlyComparison(getMonthlyComparison(vendorId))
            .orderVolume(getOrderVolumeTrends(vendorId))
            .popularMenuItems(getPopularMenuItems(vendorId, 6))
            .revenueTrends(getRevenueTrends(vendorId))
            .recentOrders(getRecentOrders(vendorId, 10))
            .recentActivities(getRecentActivities(vendorId, 10))
            .build();
    }

    /**
     * Calculate time ago string from ISO date string
     */
    private String calculateTimeAgo(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return "recently";
        }

        try {
            LocalDateTime then = LocalDateTime.parse(dateString.substring(0, 19));
            LocalDateTime now = LocalDateTime.now();

            long minutes = ChronoUnit.MINUTES.between(then, now);
            if (minutes < 60) {
                return minutes <= 1 ? "1 minute ago" : minutes + " minutes ago";
            }

            long hours = ChronoUnit.HOURS.between(then, now);
            if (hours < 24) {
                return hours == 1 ? "1 hour ago" : hours + " hours ago";
            }

            long days = ChronoUnit.DAYS.between(then, now);
            if (days < 30) {
                return days == 1 ? "1 day ago" : days + " days ago";
            }

            long months = ChronoUnit.MONTHS.between(then, now);
            return months == 1 ? "1 month ago" : months + " months ago";

        } catch (Exception e) {
            log.warn("Error calculating time ago for date: {}", dateString);
            return "recently";
        }
    }
}

