package com.tcon.webid.service;

import com.tcon.webid.dto.MenuItemReviewRequestDto;
import com.tcon.webid.entity.MenuItemReview;
import java.util.List;

public interface MenuItemReviewService {
    MenuItemReview addReview(MenuItemReviewRequestDto dto);
    List<MenuItemReview> getReviewsByMenuItem(String menuItemId);
    void deleteReview(String id);
}
