package com.tcon.webid.service;

import com.tcon.webid.dto.OrderRequestDto;
import com.tcon.webid.dto.NotificationRequestDto;
import com.tcon.webid.entity.Bid;
import com.tcon.webid.entity.Order;
import com.tcon.webid.repository.BidRepository;
import com.tcon.webid.repository.OrderRepository;
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

        // Create a Bid entity for each vendor ("requested" status)
        if (vendorOrganizationIds != null && !vendorOrganizationIds.isEmpty()) {
            for (String vendorOrgId : vendorOrganizationIds) {
                Bid bid = new Bid();
                bid.setOrderId(order.getId());
                bid.setVendorOrganizationId(vendorOrgId);
                bid.setStatus("requested");
                bid.setSubmittedAt(Instant.now().toString());
                bid.setUpdatedAt(bid.getSubmittedAt());
                bidRepo.save(bid);

                // Send notification to vendor
                NotificationRequestDto notification = new NotificationRequestDto();
                notification.setRecipientVendorOrgId(vendorOrgId);
                notification.setType("BID_REQUESTED");
                notification.setMessage("You have received a new bid request from a customer.");
                notification.setDataId(order.getId());
                notification.setDataType("order");
                notificationService.createNotification(notification);
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
        order.setStatus(newStatus);
        order.setUpdatedAt(Instant.now().toString());
        Order updatedOrder = orderRepo.save(order);

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
        }

        return updatedOrder;
    }

    @Override
    public void deleteOrder(String id) {
        orderRepo.deleteById(id);
    }
}