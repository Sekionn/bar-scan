package com.juuls_trinkets.bar_scan.dto;

import java.util.UUID;

import com.juuls_trinkets.bar_scan.model.Shop;

public record ShopResponseDTO(UUID id, UUID companyId, String name) {
    public ShopResponseDTO(Shop shop) {
        this(shop.getId(), shop.getCompanyId(), shop.getName());
    }
}
