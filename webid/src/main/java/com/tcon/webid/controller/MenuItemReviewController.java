package com.tcon.webid.controller;

import com.tcon.webid.dto.MenuItemReviewRequestDto;
import com.tcon.webid.entity.MenuItem;
import com.tcon.webid.entity.MenuItemReview;
import com.tcon.webid.repository.MenuItemRepository;
import com.tcon.webid.service.MenuItemReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vendor/{vendorOrganizationId}/menu/{menuItemId}/review")
public class MenuItemReviewController {

    @Autowired
    private MenuItemReviewService service;

    @Autowired
    private MenuItemRepository menuItemRepo;

    @PostMapping
    public MenuItemReview addReview(
            @PathVariable String vendorOrganizationId,
            @PathVariable String menuItemId,
            @RequestBody MenuItemReviewRequestDto dto
    ) {
        MenuItem menuItem = menuItemRepo.findById(menuItemId)
                .orElseThrow(() -> new IllegalArgumentException("MenuItem not found"));
        if (!menuItem.getVendorOrganizationId().equals(vendorOrganizationId)) {
            throw new IllegalArgumentException("vendorOrganizationId does not match menuItem");
        }
        dto.setMenuItemId(menuItemId);
        return service.addReview(dto);
    }

    @GetMapping
    public List<MenuItemReview> getReviewsByMenuItem(
            @PathVariable String vendorOrganizationId,
            @PathVariable String menuItemId
    ) {
        MenuItem menuItem = menuItemRepo.findById(menuItemId)
                .orElseThrow(() -> new IllegalArgumentException("MenuItem not found"));
        if (!menuItem.getVendorOrganizationId().equals(vendorOrganizationId)) {
            throw new IllegalArgumentException("vendorOrganizationId does not match menuItem");
        }
        return service.getReviewsByMenuItem(menuItemId);
    }

    @DeleteMapping("/{reviewId}")
    public void deleteReview(
            @PathVariable String vendorOrganizationId,
            @PathVariable String menuItemId,
            @PathVariable String reviewId
    ) {
        MenuItem menuItem = menuItemRepo.findById(menuItemId)
                .orElseThrow(() -> new IllegalArgumentException("MenuItem not found"));
        if (!menuItem.getVendorOrganizationId().equals(vendorOrganizationId)) {
            throw new IllegalArgumentException("vendorOrganizationId does not match menuItem");
        }
        service.deleteReview(reviewId);
    }
}
