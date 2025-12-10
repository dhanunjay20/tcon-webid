package com.tcon.webid.service;

import com.tcon.webid.dto.OrderRequestDto;
import com.tcon.webid.dto.OrderUpdateNotification;
import com.tcon.webid.dto.BidUpdateNotification;
import com.tcon.webid.dto.NotificationRequestDto;
import com.tcon.webid.entity.Bid;
import com.tcon.webid.entity.Order;
import com.tcon.webid.entity.User;
import com.tcon.webid.entity.Vendor;
import com.tcon.webid.repository.BidRepository;
import com.tcon.webid.repository.OrderRepository;
import com.tcon.webid.repository.UserRepository;
import com.tcon.webid.repository.VendorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepo;

    @Autowired
    private BidRepository bidRepo;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private RealTimeNotificationService realTimeNotificationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VendorRepository vendorRepository;

    @Override
    public Order createOrder(OrderRequestDto dto, List<String> vendorOrganizationIds) {
        Order order = new Order();
        order.setCustomerId(dto.getCustomerId());
        order.setEventName(dto.getEventName());
        order.setEventDate(dto.getEventDate());
        order.setEventLocation(dto.getEventLocation());
        order.setGuestCount(dto.getGuestCount());
        // Map only menuItemId and specialRequest from the request DTO
        order.setMenuItems(dto.getMenuItems().stream().map(mi -> {
            Order.OrderMenuItem item = new Order.OrderMenuItem();
            item.setMenuItemId(mi.getMenuItemId());
            // name/quantity/price are intentionally not set here (request DTO doesn't include them)
            item.setSpecialRequest(mi.getSpecialRequest());
            return item;
        }).toList());
        order.setTotalPrice(dto.getTotalPrice());
        order.setStatus("pending");
        order.setCreatedAt(Instant.now().toString());
        order.setUpdatedAt(order.getCreatedAt());
        order = orderRepo.save(order);

        // Send real-time WebSocket notification for order creation
        OrderUpdateNotification orderCreate = OrderUpdateNotification.builder()
                .orderId(order.getId())
                .customerId(order.getCustomerId())
                .eventName(order.getEventName())
                .eventDate(order.getEventDate())
                .eventLocation(order.getEventLocation())
                .guestCount(order.getGuestCount())
                .status(order.getStatus())
                .totalPrice(order.getTotalPrice())
                .eventType("ORDER_CREATED")
                .message("New order created: " + order.getEventName())
                .build();
        realTimeNotificationService.sendOrderUpdateToUser(order.getCustomerId(), orderCreate);
        realTimeNotificationService.broadcastOrderUpdate(orderCreate);

        // Fetch customer name once
        String customerName = null;
        if (dto.getCustomerId() != null) {
            User user = userRepository.findById(dto.getCustomerId()).orElse(null);
            if (user != null) customerName = user.getFirstName() + (user.getLastName() != null ? " " + user.getLastName() : "");
        }

        // Create a Bid entity for each vendor ("requested" status)
        if (vendorOrganizationIds != null && !vendorOrganizationIds.isEmpty()) {
            for (String vendorOrgId : vendorOrganizationIds) {
                Bid bid = new Bid();
                bid.setOrderId(order.getId());
                bid.setVendorOrganizationId(vendorOrgId);
                bid.setStatus("requested");
                bid.setSubmittedAt(Instant.now().toString());
                bid.setUpdatedAt(bid.getSubmittedAt());

                // snapshot fields
                bid.setEventName(order.getEventName());
                bid.setCustomerName(customerName);
                Vendor vendor = vendorRepository.findByVendorOrganizationId(vendorOrgId).orElse(null);
                if (vendor != null) bid.setVendorBusinessName(vendor.getBusinessName());

                bidRepo.save(bid);

                // Send notification to vendor
                NotificationRequestDto notification = new NotificationRequestDto();
                notification.setRecipientVendorOrgId(vendorOrgId);
                notification.setType("BID_REQUESTED");
                notification.setMessage("You have received a new bid request from a customer.");
                notification.setDataId(order.getId());
                notification.setDataType("order");
                notificationService.createNotification(notification);

                // Get vendor MongoDB ID
                String vendorId = vendor != null ? vendor.getId() : null;

                // Send real-time WebSocket notification to vendor about new bid request
                BidUpdateNotification bidRequest = BidUpdateNotification.builder()
                        .bidId(bid.getId())
                        .orderId(bid.getOrderId())
                        .vendorId(vendorId)
                        .vendorOrganizationId(bid.getVendorOrganizationId())
                        .status(bid.getStatus())
                        .eventType("BID_CREATED")
                        .message("New bid request for " + bid.getEventName())
                        .proposedTotalPrice(0.0)
                        .customerName(bid.getCustomerName())
                        .vendorBusinessName(bid.getVendorBusinessName())
                        .eventName(bid.getEventName())
                        .build();

                if (vendorId != null) {
                    realTimeNotificationService.sendBidUpdateToVendor(vendorId, bidRequest);
                }
                realTimeNotificationService.broadcastBidUpdate(bidRequest);
            }
        }

        return order;
    }

    @Override
    public Order getOrderById(String id) {
        return orderRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }

    @Override
    public List<Order> getOrdersByCustomer(String customerId) {
        return orderRepo.findByCustomerId(customerId);
    }

    @Override
    public List<Order> getOrdersByIds(List<String> orderIds) {
        if (orderIds == null || orderIds.isEmpty()) {
            return List.of();
        }
        return orderRepo.findAllById(orderIds);
    }

    @Override
    public List<Order> getOrdersByVendor(String vendorOrganizationId) {
        // Get all bids for this vendor
        List<Bid> vendorBids = bidRepo.findByVendorOrganizationId(vendorOrganizationId);

        // Extract unique order IDs from bids
        List<String> orderIds = vendorBids.stream()
                .map(Bid::getOrderId)
                .distinct()
                .collect(Collectors.toList());

        // Return orders by IDs
        return getOrdersByIds(orderIds);
    }

    @Override
    public Order updateOrderStatus(String id, String newStatus) {
        Order order = getOrderById(id);
        String oldStatus = order.getStatus();
        order.setStatus(newStatus);
        order.setUpdatedAt(Instant.now().toString());
        Order updatedOrder = orderRepo.save(order);

        // Get vendor MongoDB ID if available
        String vendorId = getVendorIdFromOrgId(updatedOrder.getVendorOrganizationId());

        // Send real-time WebSocket notification for order status update to customer
        OrderUpdateNotification orderStatusUpdate = OrderUpdateNotification.builder()
                .orderId(updatedOrder.getId())
                .customerId(updatedOrder.getCustomerId())
                .vendorId(vendorId)
                .vendorOrganizationId(updatedOrder.getVendorOrganizationId())
                .eventName(updatedOrder.getEventName())
                .eventDate(updatedOrder.getEventDate())
                .eventLocation(updatedOrder.getEventLocation())
                .guestCount(updatedOrder.getGuestCount())
                .status(updatedOrder.getStatus())
                .totalPrice(updatedOrder.getTotalPrice())
                .eventType("ORDER_STATUS_CHANGED")
                .message("Order " + updatedOrder.getEventName() + " status changed from " + oldStatus + " to " + newStatus)
                .build();
        realTimeNotificationService.sendOrderUpdateToUser(updatedOrder.getCustomerId(), orderStatusUpdate);
        realTimeNotificationService.broadcastOrderUpdate(orderStatusUpdate);

        // Notify all vendors who have bids for this order
        List<Bid> orderBids = bidRepo.findByOrderId(id);
        for (Bid bid : orderBids) {
            NotificationRequestDto notification = new NotificationRequestDto();
            notification.setRecipientVendorOrgId(bid.getVendorOrganizationId());
            notification.setType("ORDER_STATUS");
            notification.setMessage("Order " + order.getEventName() + " status changed to " + newStatus);
            notification.setDataId(order.getId());
            notification.setDataType("order");
            notificationService.createNotification(notification);

            // Get vendor MongoDB ID for this bid
            String bidVendorId = getVendorIdFromOrgId(bid.getVendorOrganizationId());

            // Send real-time WebSocket notification to vendor
            if (bidVendorId != null) {
                realTimeNotificationService.sendOrderUpdateToVendor(bidVendorId, orderStatusUpdate);
            }
        }

        return updatedOrder;
    }

    @Override
    public void deleteOrder(String id) {
        Order order = getOrderById(id);
        orderRepo.deleteById(id);

        // Get vendor MongoDB ID if available
        String vendorId = getVendorIdFromOrgId(order.getVendorOrganizationId());

        // Send real-time WebSocket notification for order deletion
        OrderUpdateNotification orderDelete = OrderUpdateNotification.builder()
                .orderId(order.getId())
                .customerId(order.getCustomerId())
                .vendorId(vendorId)
                .vendorOrganizationId(order.getVendorOrganizationId())
                .eventName(order.getEventName())
                .eventDate(order.getEventDate())
                .eventLocation(order.getEventLocation())
                .guestCount(order.getGuestCount())
                .status("deleted")
                .totalPrice(order.getTotalPrice())
                .eventType("ORDER_DELETED")
                .message("Order " + order.getEventName() + " has been deleted")
                .build();
        realTimeNotificationService.sendOrderUpdateToUser(order.getCustomerId(), orderDelete);
        realTimeNotificationService.broadcastOrderUpdate(orderDelete);

        // Notify all vendors who have bids for this order
        List<Bid> orderBids = bidRepo.findByOrderId(id);
        for (Bid bid : orderBids) {
            String bidVendorId = getVendorIdFromOrgId(bid.getVendorOrganizationId());
            if (bidVendorId != null) {
                realTimeNotificationService.sendOrderUpdateToVendor(bidVendorId, orderDelete);
            }
        }
    }

    /**
     * Helper method to get vendor MongoDB _id from vendorOrganizationId
     */
    private String getVendorIdFromOrgId(String vendorOrganizationId) {
        if (vendorOrganizationId == null) {
            return null;
        }
        return vendorRepository.findByVendorOrganizationId(vendorOrganizationId)
                .map(Vendor::getId)
                .orElse(null);
    }
}