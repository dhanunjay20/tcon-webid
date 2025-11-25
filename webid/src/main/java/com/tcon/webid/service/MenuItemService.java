package com.tcon.webid.service;

import com.tcon.webid.dto.MenuItemRequestDto;
import com.tcon.webid.dto.MenuItemResponseDto;
import java.util.List;

public interface MenuItemService {
    MenuItemResponseDto createMenuItem(MenuItemRequestDto dto);
    MenuItemResponseDto getMenuItemById(String id);
    List<MenuItemResponseDto> getMenuItemsByVendor(String vendorOrganizationId);
    MenuItemResponseDto updateMenuItem(String id, MenuItemRequestDto dto);
    void deleteMenuItem(String id);
}
