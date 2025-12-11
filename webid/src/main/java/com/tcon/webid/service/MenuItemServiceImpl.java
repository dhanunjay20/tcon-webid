package com.tcon.webid.service;

import com.tcon.webid.dto.MenuItemRequestDto;
import com.tcon.webid.dto.MenuItemResponseDto;
import com.tcon.webid.entity.MenuItem;
import com.tcon.webid.repository.MenuItemRepository;
import com.tcon.webid.service.MenuItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MenuItemServiceImpl implements MenuItemService {

    @Autowired
    private MenuItemRepository repo;

    @Override
    public MenuItemResponseDto createMenuItem(MenuItemRequestDto dto) {
        MenuItem item = toEntity(dto);
        MenuItem saved = repo.save(item);
        return toResponseDto(saved);
    }

    @Override
    public MenuItemResponseDto getMenuItemById(String id) {
        return repo.findById(id)
                .map(this::toResponseDto)
                .orElseThrow(() -> new RuntimeException("Menu item not found"));
    }

    @Override
    public List<MenuItemResponseDto> getMenuItemsByVendor(String vendorOrganizationId) {
        return repo.findByVendorOrganizationId(vendorOrganizationId)
                .stream().map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public MenuItemResponseDto updateMenuItem(String id, MenuItemRequestDto dto) {
        MenuItem item = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Menu item not found"));
        item.setVendorOrganizationId(dto.getVendorOrganizationId());
        item.setName(dto.getName());
        item.setDescription(dto.getDescription());
        item.setImages(dto.getImages());
        item.setCategory(dto.getCategory());
        item.setSubCategory(dto.getSubCategory());
        item.setIngredients(dto.getIngredients());
        item.setSpiceLevels(dto.getSpiceLevels());
        item.setAvailable(dto.isAvailable());
        return toResponseDto(repo.save(item));
    }

    @Override
    public void deleteMenuItem(String id) {
        repo.deleteById(id);
    }

    private MenuItem toEntity(MenuItemRequestDto dto) {
        MenuItem item = new MenuItem();
        item.setVendorOrganizationId(dto.getVendorOrganizationId());
        item.setName(dto.getName());
        item.setDescription(dto.getDescription());
        item.setImages(dto.getImages());
        item.setCategory(dto.getCategory());
        item.setSubCategory(dto.getSubCategory());
        item.setIngredients(dto.getIngredients());
        item.setSpiceLevels(dto.getSpiceLevels());
        item.setAvailable(dto.isAvailable());
        return item;
    }

    private MenuItemResponseDto toResponseDto(MenuItem item) {
        MenuItemResponseDto dto = new MenuItemResponseDto();
        dto.setId(item.getId());
        dto.setVendorOrganizationId(item.getVendorOrganizationId());
        dto.setName(item.getName());
        dto.setDescription(item.getDescription());
        dto.setImages(item.getImages());
        dto.setCategory(item.getCategory());
        dto.setSubCategory(item.getSubCategory());
        dto.setIngredients(item.getIngredients());
        dto.setSpiceLevels(item.getSpiceLevels());
        dto.setAvailable(item.isAvailable());
        return dto;
    }
}
