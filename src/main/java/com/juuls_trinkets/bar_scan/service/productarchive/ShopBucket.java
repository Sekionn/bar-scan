package com.juuls_trinkets.bar_scan.service.productarchive;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import com.juuls_trinkets.bar_scan.dto.ProductArchiveShopResponseDTO;

class ShopBucket {
    final UUID shopId;
    final String shopName;
    final Map<Integer, YearBucket> years = new LinkedHashMap<>();
    long productCount;

    ShopBucket(UUID shopId, String shopName) {
        this.shopId = shopId;
        this.shopName = shopName;
    }

    ProductArchiveShopResponseDTO toResponse() {
        return new ProductArchiveShopResponseDTO(
                shopId,
                shopName,
                productCount,
                years.values().stream()
                        .sorted(Comparator.comparing(YearBucket::year).reversed())
                        .map(YearBucket::toResponse)
                        .toList()
        );
    }
}
