package com.tcon.webid.service;

import com.tcon.webid.entity.MenuItem;
import com.tcon.webid.repository.MenuItemAggregationRepository;
import com.tcon.webid.service.MenuItemGlobalQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class MenuItemGlobalQueryServiceImpl implements MenuItemGlobalQueryService {
    @Autowired
    private MenuItemAggregationRepository aggRepo;

    @Override
    public List<MenuItem> getUniqueMenuItemsAllFields() {
        return aggRepo.findAllUniqueMenuItemsByNameAndCategory();
    }
}
