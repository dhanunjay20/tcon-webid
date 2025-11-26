package com.tcon.webid.controller;

import com.tcon.webid.entity.MenuItem;
import com.tcon.webid.service.MenuItemGlobalQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/global-menu")
public class MenuItemGlobalQueryController {

    @Autowired
    private MenuItemGlobalQueryService service;

    @GetMapping("/unique")
    public List<MenuItem> getUniqueMenuItems() {
        return service.getUniqueMenuItemsAllFields();
    }
}
