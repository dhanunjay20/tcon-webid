package com.tcon.webid.service;

import com.tcon.webid.dto.BidRequestDto;
import com.tcon.webid.dto.BidUpdateNotification;
import com.tcon.webid.dto.NotificationRequestDto;
import com.tcon.webid.entity.Bid;
import com.tcon.webid.entity.Order;
import com.tcon.webid.entity.Vendor;
import com.tcon.webid.repository.BidRepository;
import com.tcon.webid.repository.OrderRepository;
import com.tcon.webid.repository.VendorRepository;
import com.tcon.webid.service.BidService;
import com.tcon.webid.service.NotificationService;
import com.tcon.webid.service.RealTimeNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class BidServiceImpl implements BidService {

    @Autowired
    private BidRepository bidRepo;
    @Autowired
    private OrderRepository orderRepo;
    @Autowired
    private VendorRepository vendorRepo;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private RealTimeNotificationService realTimeNotificationService;

    @Override
    public Bid submitBidQuote(String bidId, BidRequestDto dto) {
        Bid bid = bidRepo.findById(bidId).orElseThrow(() -> new RuntimeException("Bid not found"));
        bid.setProposedMessage(dto.getProposedMessage());
        bid.setProposedTotalPrice(dto.getProposedTotalPrice());

        // Update snapshot fields if provided by DTO (do not overwrite existing values with nulls)
        if (dto.getCustomerName() != null && !dto.getCustomerName().isBlank()) {
            bid.setCustomerName(dto.getCustomerName());
        }
        if (dto.getVendorBusinessName() != null && !dto.getVendorBusinessName().isBlank()) {
            bid.setVendorBusinessName(dto.getVendorBusinessName());
        }
        if (dto.getEventName() != null && !dto.getEventName().isBlank()) {
            bid.setEventName(dto.getEventName());
        }

        bid.setStatus("quoted");
        bid.setUpdatedAt(Instant.now().toString());
        Bid savedBid = bidRepo.save(bid);

        // Update order totalPrice when bid is quoted
        Order order = orderRepo.findById(bid.getOrderId()).orElseThrow(() -> new RuntimeException("Order not found"));
        order.setTotalPrice(bid.getProposedTotalPrice());
        order.setUpdatedAt(Instant.now().toString());
        orderRepo.save(order);

        // Notify user customer about vendor quote
        NotificationRequestDto notification = new NotificationRequestDto();
        notification.setRecipientUserId(order.getCustomerId());
        notification.setType("BID_QUOTED");
        notification.setMessage("A vendor has submitted a quote for your event.");
        notification.setDataId(order.getId());
        notification.setDataType("order");
        notificationService.createNotification(notification);

        // Send real-time WebSocket notification to user
        BidUpdateNotification bidUpdate = BidUpdateNotification.builder()
                .bidId(savedBid.getId())
                .orderId(savedBid.getOrderId())
                .vendorOrganizationId(savedBid.getVendorOrganizationId())
                .status(savedBid.getStatus())
                .eventType("BID_QUOTED")
                .message("A vendor has submitted a quote for your event: " + savedBid.getEventName())
                .proposedTotalPrice(savedBid.getProposedTotalPrice())
                .customerName(savedBid.getCustomerName())
                .vendorBusinessName(savedBid.getVendorBusinessName())
                .eventName(savedBid.getEventName())
                .build();
        realTimeNotificationService.sendBidUpdateToUser(order.getCustomerId(), bidUpdate);
        realTimeNotificationService.broadcastBidUpdate(bidUpdate);

        return savedBid;
    }

    @Override
    public Bid getBidById(String id) { return bidRepo.findById(id).orElseThrow(() -> new RuntimeException("Bid not found")); }
    @Override
    public List<Bid> getBidsByOrder(String orderId) { return bidRepo.findByOrderId(orderId); }
    @Override
    public List<Bid> getBidsByVendor(String vendorOrgId) { return bidRepo.findByVendorOrganizationId(vendorOrgId); }

    @Override
    public Bid acceptBid(String bidId) {
        Bid bid = getBidById(bidId);
        bid.setStatus("accepted");
        bid.setUpdatedAt(Instant.now().toString());
        Bid savedBid = bidRepo.save(bid);

        // Update order: assign vendor, status "confirmed"
        Order order = orderRepo.findById(bid.getOrderId()).orElseThrow(() -> new RuntimeException("Order not found"));
        order.setVendorOrganizationId(bid.getVendorOrganizationId());
        order.setStatus("confirmed");
        order.setUpdatedAt(Instant.now().toString());
        orderRepo.save(order);

        // Notify accepted vendor
        NotificationRequestDto acceptNotification = new NotificationRequestDto();
        acceptNotification.setRecipientVendorOrgId(bid.getVendorOrganizationId());
        acceptNotification.setType("BID_ACCEPTED");
        acceptNotification.setMessage("Your quote has been accepted!");
        acceptNotification.setDataId(bid.getOrderId());
        acceptNotification.setDataType("order");
        notificationService.createNotification(acceptNotification);

        // Get vendor MongoDB ID
        String vendorId = getVendorIdFromOrgId(savedBid.getVendorOrganizationId());

        // Send real-time WebSocket notification to accepted vendor
        BidUpdateNotification acceptUpdate = BidUpdateNotification.builder()
                .bidId(savedBid.getId())
                .orderId(savedBid.getOrderId())
                .vendorId(vendorId)
                .vendorOrganizationId(savedBid.getVendorOrganizationId())
                .status(savedBid.getStatus())
                .eventType("BID_ACCEPTED")
                .message("Your quote has been accepted for " + savedBid.getEventName())
                .proposedTotalPrice(savedBid.getProposedTotalPrice())
                .customerName(savedBid.getCustomerName())
                .vendorBusinessName(savedBid.getVendorBusinessName())
                .eventName(savedBid.getEventName())
                .build();

        if (vendorId != null) {
            realTimeNotificationService.sendBidUpdateToVendor(vendorId, acceptUpdate);
        }
        realTimeNotificationService.sendBidUpdateToUser(order.getCustomerId(), acceptUpdate);
        realTimeNotificationService.broadcastBidUpdate(acceptUpdate);

        // Notify rejected vendors
        rejectOtherBids(bid.getOrderId(), bidId);

        return savedBid;
    }

    @Override
    public Bid rejectBid(String bidId) {
        Bid bid = getBidById(bidId);

        if (!"pending".equalsIgnoreCase(bid.getStatus()) && !"quoted".equalsIgnoreCase(bid.getStatus())) {
            throw new IllegalStateException("Only pending or quoted bids can be rejected");
        }

        bid.setStatus("rejected");
        bid.setUpdatedAt(Instant.now().toString());
        Bid savedBid = bidRepo.save(bid);

        // Notify vendor about rejection
        NotificationRequestDto notification = new NotificationRequestDto();
        notification.setRecipientVendorOrgId(bid.getVendorOrganizationId());
        notification.setType("BID_REJECTED");
        notification.setMessage("Your quote was not selected for this event.");
        notification.setDataId(bid.getOrderId());
        notification.setDataType("order");
        notificationService.createNotification(notification);

        // Send real-time WebSocket notification to rejected vendor
        BidUpdateNotification rejectUpdate = BidUpdateNotification.builder()
                .bidId(savedBid.getId())
                .orderId(savedBid.getOrderId())
                .vendorOrganizationId(savedBid.getVendorOrganizationId())
                .status(savedBid.getStatus())
                .eventType("BID_REJECTED")
                .message("Your quote was not selected for " + savedBid.getEventName())
                .proposedTotalPrice(savedBid.getProposedTotalPrice())
                .customerName(savedBid.getCustomerName())
                .vendorBusinessName(savedBid.getVendorBusinessName())
                .eventName(savedBid.getEventName())
                .build();
        realTimeNotificationService.sendBidUpdateToVendor(bid.getVendorOrganizationId(), rejectUpdate);
        realTimeNotificationService.broadcastBidUpdate(rejectUpdate);

        // Update order status to 'rejected' if there are no remaining active bids for this order
        Order order = orderRepo.findById(bid.getOrderId()).orElse(null);
        if (order != null) {
            // Check if any other bid for this order is still pending/quoted/accepted
            List<Bid> otherBids = bidRepo.findByOrderId(order.getId());
            boolean hasActive = otherBids.stream()
                    .anyMatch(b -> !b.getId().equals(savedBid.getId()) && (
                            "pending".equalsIgnoreCase(b.getStatus()) ||
                            "quoted".equalsIgnoreCase(b.getStatus()) ||
                            "accepted".equalsIgnoreCase(b.getStatus())
                    ));

            if (!hasActive) {
                // Only mark order as rejected if it is not already confirmed
                if (!"confirmed".equalsIgnoreCase(order.getStatus())) {
                    order.setStatus("rejected");
                    order.setUpdatedAt(Instant.now().toString());
                    orderRepo.save(order);
                }
            }
        }

        return savedBid;
    }

    @Override
    public void rejectOtherBids(String orderId, String acceptedBidId) {
        List<Bid> bids = getBidsByOrder(orderId);
        for (Bid b : bids) {
            if (!b.getId().equals(acceptedBidId) && !"rejected".equals(b.getStatus())) {
                b.setStatus("rejected");
                b.setUpdatedAt(Instant.now().toString());
                bidRepo.save(b);

                // Notify each rejected vendor
                NotificationRequestDto rejectNotification = new NotificationRequestDto();
                rejectNotification.setRecipientVendorOrgId(b.getVendorOrganizationId());
                rejectNotification.setType("BID_REJECTED");
                rejectNotification.setMessage("Your quote was not selected for this event.");
                rejectNotification.setDataId(b.getOrderId());
                rejectNotification.setDataType("order");
                notificationService.createNotification(rejectNotification);

                // Get vendor MongoDB ID
                String vendorId = getVendorIdFromOrgId(b.getVendorOrganizationId());

                // Send real-time WebSocket notification to each rejected vendor
                BidUpdateNotification rejectUpdate = BidUpdateNotification.builder()
                        .bidId(b.getId())
                        .orderId(b.getOrderId())
                        .vendorId(vendorId)
                        .vendorOrganizationId(b.getVendorOrganizationId())
                        .status(b.getStatus())
                        .eventType("BID_REJECTED")
                        .message("Your quote was not selected for " + b.getEventName())
                        .proposedTotalPrice(b.getProposedTotalPrice())
                        .customerName(b.getCustomerName())
                        .vendorBusinessName(b.getVendorBusinessName())
                        .eventName(b.getEventName())
                        .build();

                if (vendorId != null) {
                    realTimeNotificationService.sendBidUpdateToVendor(vendorId, rejectUpdate);
                }
                realTimeNotificationService.broadcastBidUpdate(rejectUpdate);
            }
        }
    }

    @Override
    public void deleteBid(String id) {
        Bid bid = getBidById(id);
        bidRepo.deleteById(id);

        // Get vendor MongoDB ID
        String vendorId = getVendorIdFromOrgId(bid.getVendorOrganizationId());

        // Send real-time WebSocket notification about bid deletion
        BidUpdateNotification deleteUpdate = BidUpdateNotification.builder()
                .bidId(bid.getId())
                .orderId(bid.getOrderId())
                .vendorId(vendorId)
                .vendorOrganizationId(bid.getVendorOrganizationId())
                .status("deleted")
                .eventType("BID_DELETED")
                .message("Bid for " + bid.getEventName() + " has been deleted")
                .proposedTotalPrice(bid.getProposedTotalPrice())
                .customerName(bid.getCustomerName())
                .vendorBusinessName(bid.getVendorBusinessName())
                .eventName(bid.getEventName())
                .build();

        if (vendorId != null) {
            realTimeNotificationService.sendBidUpdateToVendor(vendorId, deleteUpdate);
        }
        realTimeNotificationService.broadcastBidUpdate(deleteUpdate);
    }

    /**
     * Helper method to get vendor MongoDB _id from vendorOrganizationId
     */
    private String getVendorIdFromOrgId(String vendorOrganizationId) {
        if (vendorOrganizationId == null) {
            return null;
        }
        return vendorRepo.findByVendorOrganizationId(vendorOrganizationId)
                .map(Vendor::getId)
                .orElse(null);
    }
}
