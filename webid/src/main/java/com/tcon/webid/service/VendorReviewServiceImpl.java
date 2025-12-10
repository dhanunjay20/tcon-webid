package com.tcon.webid.service;

import com.tcon.webid.dto.VendorReviewRequestDto;
import com.tcon.webid.entity.VendorReview;
import com.tcon.webid.repository.VendorReviewRepository;
import com.tcon.webid.service.VendorReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class VendorReviewServiceImpl implements VendorReviewService {

    @Autowired
    private VendorReviewRepository repo;

    @Override
    public VendorReview addReview(VendorReviewRequestDto dto) {
        VendorReview review = new VendorReview();
        review.setVendorOrganizationId(dto.getVendorOrganizationId());
        review.setCustomerName(dto.getCustomerName());
        review.setReviewDate(dto.getReviewDate());
        review.setDescription(dto.getDescription());
        review.setStars(dto.getStars());
        return repo.save(review);
    }

    @Override
    public List<VendorReview> getReviewsByVendor(String vendorOrganizationId) {
        return repo.findByVendorOrganizationId(vendorOrganizationId);
    }

    @Override
    public void deleteReview(String id) {
        repo.deleteById(id);
    }

    @Override
    public List<VendorReview> getLatestReviewsByVendor(String vendorOrganizationId) {
        return repo.findTop10ByVendorOrganizationIdOrderByReviewDateDesc(vendorOrganizationId);
    }
}
