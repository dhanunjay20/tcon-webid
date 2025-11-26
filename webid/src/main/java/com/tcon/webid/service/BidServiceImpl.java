package com.tcon.webid.service;

import com.tcon.webid.dto.BidRequestDto;
import com.tcon.webid.dto.NotificationRequestDto;
import com.tcon.webid.entity.Bid;
import com.tcon.webid.entity.Order;
import com.tcon.webid.repository.BidRepository;
import com.tcon.webid.repository.OrderRepository;
import com.tcon.webid.service.BidService;
import com.tcon.webid.service.NotificationService;
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
    private NotificationService notificationService;

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

        // Notify user customer about vendor quote
        Order order = orderRepo.findById(bid.getOrderId()).orElseThrow(() -> new RuntimeException("Order not found"));
        NotificationRequestDto notification = new NotificationRequestDto();
        notification.setRecipientUserId(order.getCustomerId());
        notification.setType("BID_QUOTED");
        notification.setMessage("A vendor has submitted a quote for your event.");
        notification.setDataId(order.getId());
        notification.setDataType("order");
        notificationService.createNotification(notification);

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
            }
        }
    }

    @Override
    public void deleteBid(String id) { bidRepo.deleteById(id); }
}
