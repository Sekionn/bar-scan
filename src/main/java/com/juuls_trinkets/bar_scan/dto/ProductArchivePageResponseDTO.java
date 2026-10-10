package com.juuls_trinkets.bar_scan.dto;

import java.util.List;

public record ProductArchivePageResponseDTO(
        List<ProductArchiveItemResponseDTO> products,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
