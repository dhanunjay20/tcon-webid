package com.tcon.webid.service;

import com.tcon.webid.dto.VendorReviewRequestDto;
import com.tcon.webid.entity.VendorReview;
import java.util.List;

public interface VendorReviewService {
    VendorReview addReview(VendorReviewRequestDto dto);
    List<VendorReview> getReviewsByVendor(String vendorOrganizationId);
    void deleteReview(String id);
}
