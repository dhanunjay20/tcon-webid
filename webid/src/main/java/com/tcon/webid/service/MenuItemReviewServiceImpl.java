package com.tcon.webid.service;

import com.tcon.webid.dto.MenuItemReviewRequestDto;
import com.tcon.webid.entity.MenuItemReview;
import com.tcon.webid.repository.MenuItemReviewRepository;
import com.tcon.webid.service.MenuItemReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class MenuItemReviewServiceImpl implements MenuItemReviewService {

    @Autowired
    private MenuItemReviewRepository repo;

    @Override
    public MenuItemReview addReview(MenuItemReviewRequestDto dto) {
        MenuItemReview review = new MenuItemReview();
        review.setMenuItemId(dto.getMenuItemId());
        review.setCustomerName(dto.getCustomerName());
        review.setReviewDate(dto.getReviewDate());
        review.setDescription(dto.getDescription());
        review.setStars(dto.getStars());
        return repo.save(review);
    }

    @Override
    public List<MenuItemReview> getReviewsByMenuItem(String menuItemId) {
        return repo.findByMenuItemId(menuItemId);
    }

    @Override
    public void deleteReview(String id) {
        repo.deleteById(id);
    }
}
