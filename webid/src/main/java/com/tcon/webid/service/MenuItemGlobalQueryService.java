package com.tcon.webid.service;

import com.tcon.webid.entity.MenuItem;
import java.util.List;

public interface MenuItemGlobalQueryService {
    List<MenuItem> getUniqueMenuItemsAllFields();
}
