package com.tcon.webid.mapper;

import com.tcon.webid.dto.MenuItemResponseDto;
import com.tcon.webid.dto.OrderMenuItemResponseDto;
import com.tcon.webid.dto.OrderResponseDto;
import com.tcon.webid.entity.MenuItem;
import com.tcon.webid.entity.Order;
import com.tcon.webid.entity.Vendor;
import com.tcon.webid.repository.MenuItemRepository;
import com.tcon.webid.repository.VendorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class OrderResponseMapper {

    @Autowired
    private MenuItemRepository menuItemRepository;

    @Autowired
    private VendorRepository vendorRepository;

    public OrderResponseDto toDto(Order order) {
        OrderResponseDto dto = new OrderResponseDto();
        dto.setId(order.getId());
        dto.setCustomerId(order.getCustomerId());
        dto.setVendorOrganizationId(order.getVendorOrganizationId());
        dto.setEventName(order.getEventName());
        dto.setEventDate(order.getEventDate());
        dto.setEventLocation(order.getEventLocation());
        dto.setGuestCount(order.getGuestCount());
        dto.setStatus(order.getStatus());
        dto.setTotalPrice(order.getTotalPrice());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());

        // populate vendor business name if vendorOrganizationId is set
        if (order.getVendorOrganizationId() != null) {
            Vendor vendor = vendorRepository.findByVendorOrganizationId(order.getVendorOrganizationId()).orElse(null);
            if (vendor != null) dto.setVendorBusinessName(vendor.getBusinessName());
        }

        // Collect unique menuItemIds from the order
        List<String> menuIds = order.getMenuItems() == null ? List.of() : order.getMenuItems().stream()
                .map(mi -> mi.getMenuItemId())
                .distinct()
                .collect(Collectors.toList());

        // Fetch menu items from repository
        List<MenuItem> menuItems = menuItemRepository.findAllById(menuIds);
        Map<String, MenuItem> menuMap = menuItems.stream().collect(Collectors.toMap(MenuItem::getId, m -> m));

        List<OrderMenuItemResponseDto> mapped = order.getMenuItems() == null ? List.of() : order.getMenuItems().stream().map(mi -> {
            OrderMenuItemResponseDto omi = new OrderMenuItemResponseDto();
            omi.setMenuItemId(mi.getMenuItemId());
            omi.setSpecialRequest(mi.getSpecialRequest());

            // Populate embedded MenuItemResponseDto
            MenuItem menuItem = menuMap.get(mi.getMenuItemId());
            if (menuItem != null) {
                MenuItemResponseDto mrd = new MenuItemResponseDto();
                mrd.setId(menuItem.getId());
                mrd.setVendorOrganizationId(menuItem.getVendorOrganizationId());
                mrd.setName(menuItem.getName());
                mrd.setDescription(menuItem.getDescription());
                mrd.setImages(menuItem.getImages());
                mrd.setCategory(menuItem.getCategory());
                mrd.setSubCategory(menuItem.getSubCategory());
                mrd.setIngredients(menuItem.getIngredients());
                mrd.setSpiceLevels(menuItem.getSpiceLevels());
                mrd.setAvailable(menuItem.isAvailable());
                omi.setMenuItem(mrd);
            }

            return omi;
        }).collect(Collectors.toList());

        dto.setMenuItems(mapped);
        return dto;
    }
}
