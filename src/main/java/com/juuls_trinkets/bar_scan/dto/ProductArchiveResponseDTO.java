package com.juuls_trinkets.bar_scan.dto;

import java.util.List;

public record ProductArchiveResponseDTO(
        List<ProductArchiveShopResponseDTO> shops
) {
}
