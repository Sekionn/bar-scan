package com.juuls_trinkets.bar_scan.dto;

import java.util.List;
import java.util.UUID;

public record ProductArchiveShopResponseDTO(
        UUID shopId,
        String shopName,
        long productCount,
        List<ProductArchiveYearResponseDTO> years
) {
}
