package com.tcon.webid.controller;

import com.tcon.webid.dto.MenuItemRequestDto;
import com.tcon.webid.dto.MenuItemResponseDto;
import com.tcon.webid.service.MenuItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/vendor/{vendorOrganizationId}/menu")
public class MenuItemController {

    @Autowired
    private MenuItemService menuItemService;

    @PostMapping
    public MenuItemResponseDto createMenuItem(
            @PathVariable String vendorOrganizationId,
            @RequestBody @Valid MenuItemRequestDto dto
    ) {
        dto.setVendorOrganizationId(vendorOrganizationId);
        return menuItemService.createMenuItem(dto);
    }

    @GetMapping
    public List<MenuItemResponseDto> getMenuItemsByVendor(@PathVariable String vendorOrganizationId) {
        return menuItemService.getMenuItemsByVendor(vendorOrganizationId);
    }

    @GetMapping("/{id}")
    public MenuItemResponseDto getMenuItem(
            @PathVariable String vendorOrganizationId,
            @PathVariable String id
    ) {
        MenuItemResponseDto item = menuItemService.getMenuItemById(id);
        if (!item.getVendorOrganizationId().equals(vendorOrganizationId)) {
            throw new IllegalArgumentException("Access denied: vendorOrganizationId mismatch");
        }
        return item;
    }

    @PutMapping("/{id}")
    public MenuItemResponseDto updateMenuItem(
            @PathVariable String vendorOrganizationId,
            @PathVariable String id,
            @RequestBody @Valid MenuItemRequestDto dto
    ) {
        dto.setVendorOrganizationId(vendorOrganizationId);
        // Validate ownership
        MenuItemResponseDto existing = menuItemService.getMenuItemById(id);
        if (!existing.getVendorOrganizationId().equals(vendorOrganizationId)) {
            throw new IllegalArgumentException("Access denied: vendorOrganizationId mismatch");
        }
        return menuItemService.updateMenuItem(id, dto);
    }

    @DeleteMapping("/{id}")
    public void deleteMenuItem(
            @PathVariable String vendorOrganizationId,
            @PathVariable String id
    ) {
        MenuItemResponseDto item = menuItemService.getMenuItemById(id);
        if (!item.getVendorOrganizationId().equals(vendorOrganizationId)) {
            throw new IllegalArgumentException("Access denied: vendorOrganizationId mismatch");
        }
        menuItemService.deleteMenuItem(id);
    }
}
