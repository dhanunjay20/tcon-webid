package com.tcon.webid.service;

import com.tcon.webid.dto.BidRequestDto;
import com.tcon.webid.entity.Bid;
import java.util.List;

/**
 * Service for vendor bids/quotes on orders.
 * Handles vendor quote submission, acceptance/rejection, and related operations.
 */
public interface BidService {
    /**
     * Submit a bid quote for an order (called by vendor)
     * @param bidId Existing bid record ID (generated when order requested)
     * @param dto Quote/price/message from vendor
     * @return The updated Bid entity
     */
    Bid submitBidQuote(String bidId, BidRequestDto dto);

    Bid getBidById(String id);

    List<Bid> getBidsByOrder(String orderId);

    List<Bid> getBidsByVendor(String vendorOrganizationId);

    /**
     * Accept a vendor bid (called by user); sets order/vendor, rejects other bids
     */
    Bid acceptBid(String bidId);

    /**
     * Reject all other bids for an order (called by service when one bid is accepted)
     */
    void rejectOtherBids(String orderId, String acceptedBidId);

    void deleteBid(String id);
}
