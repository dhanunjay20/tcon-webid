package com.tcon.webid.controller;

import com.tcon.webid.dto.BidRequestDto;
import com.tcon.webid.entity.Bid;
import com.tcon.webid.service.BidService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/vendor/{vendorOrganizationId}/bids")
public class BidController {

    @Autowired
    private BidService bidService;

    @GetMapping("/{bidId}")
    public Bid getBid(@PathVariable String vendorOrganizationId, @PathVariable String bidId) {
        Bid bid = bidService.getBidById(bidId);
        if (!bid.getVendorOrganizationId().equals(vendorOrganizationId)) {
            throw new IllegalArgumentException("Access denied");
        }
        return bid;
    }

    @GetMapping
    public List<Bid> getBidsByVendor(@PathVariable String vendorOrganizationId) {
        return bidService.getBidsByVendor(vendorOrganizationId);
    }

    @GetMapping("/order/{orderId}")
    public List<Bid> getBidsByOrder(@PathVariable String vendorOrganizationId, @PathVariable String orderId) {
        List<Bid> bids = bidService.getBidsByOrder(orderId);
        // Optionally filter by vendorOrgId
        return bids;
    }

    @PutMapping("/{bidId}/quote")
    public Bid submitBidQuote(@PathVariable String vendorOrganizationId,
                              @PathVariable String bidId,
                              @RequestBody BidRequestDto dto) {
        // Quote submission by vendor
        dto.setVendorOrganizationId(vendorOrganizationId);
        return bidService.submitBidQuote(bidId, dto);
    }

    @PutMapping("/{bidId}/accept")
    public Bid acceptBid(@PathVariable String vendorOrganizationId, @PathVariable String bidId) {
        Bid bid = bidService.getBidById(bidId);
        // Only user/organizer should call this, typically via a service method with auth
        return bidService.acceptBid(bidId);
    }

    // New: allow vendor to reject their own bid
    @PutMapping("/{bidId}/reject")
    public ResponseEntity<?> rejectBid(@PathVariable String vendorOrganizationId, @PathVariable String bidId) {
        Bid bid = bidService.getBidById(bidId);
        if (bid == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(java.util.Map.of("error", "Bid not found"));
        }
        if (!bid.getVendorOrganizationId().equals(vendorOrganizationId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(java.util.Map.of("error", "Access denied"));
        }
        // Vendors can reject bids that are pending or quoted
        String status = bid.getStatus();
        if (!"pending".equalsIgnoreCase(status) && !"quoted".equalsIgnoreCase(status)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(java.util.Map.of("error", "Only pending or quoted bids can be rejected"));
        }
        Bid rejected = bidService.rejectBid(bidId);
        return ResponseEntity.ok(rejected);
    }

    @DeleteMapping("/{bidId}")
    public void deleteBid(@PathVariable String vendorOrganizationId, @PathVariable String bidId) {
        Bid bid = bidService.getBidById(bidId);
        if (!bid.getVendorOrganizationId().equals(vendorOrganizationId)) {
            throw new IllegalArgumentException("Access denied");
        }
        bidService.deleteBid(bidId);
    }
}
