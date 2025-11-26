package com.tcon.webid.repository;

import com.tcon.webid.entity.MenuItemReview;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface MenuItemReviewRepository extends MongoRepository<MenuItemReview, String> {
    List<MenuItemReview> findByMenuItemId(String menuItemId);
}
