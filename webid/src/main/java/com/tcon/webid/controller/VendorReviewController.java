package com.tcon.webid.controller;

import com.tcon.webid.dto.VendorReviewRequestDto;
import com.tcon.webid.entity.VendorReview;
import com.tcon.webid.service.VendorReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vendor/{vendorOrganizationId}/review")
public class VendorReviewController {

    @Autowired
    private VendorReviewService service;

    @PostMapping
    public VendorReview addReview(
            @PathVariable String vendorOrganizationId,
            @RequestBody VendorReviewRequestDto dto
    ) {
        dto.setVendorOrganizationId(vendorOrganizationId);
        return service.addReview(dto);
    }

    @GetMapping
    public List<VendorReview> getReviewsByVendor(
            @PathVariable String vendorOrganizationId
    ) {
        return service.getReviewsByVendor(vendorOrganizationId);
    }

    @GetMapping("/latest")
    public List<VendorReview> getLatestReviewsByVendor(
            @PathVariable String vendorOrganizationId
    ) {
        return service.getLatestReviewsByVendor(vendorOrganizationId);
    }

    @DeleteMapping("/{reviewId}")
    public void deleteReview(
            @PathVariable String vendorOrganizationId,
            @PathVariable String reviewId
    ) {
        service.deleteReview(reviewId);
    }
}
