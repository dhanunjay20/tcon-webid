package com.tcon.webid.controller;

import com.tcon.webid.dto.BidRequestDto;
import com.tcon.webid.entity.Bid;
import com.tcon.webid.service.BidService;
import org.springframework.beans.factory.annotation.Autowired;
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

    @DeleteMapping("/{bidId}")
    public void deleteBid(@PathVariable String vendorOrganizationId, @PathVariable String bidId) {
        Bid bid = bidService.getBidById(bidId);
        if (!bid.getVendorOrganizationId().equals(vendorOrganizationId)) {
            throw new IllegalArgumentException("Access denied");
        }
        bidService.deleteBid(bidId);
    }
}
