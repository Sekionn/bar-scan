package com.juuls_trinkets.bar_scan.dto;

import java.util.List;

public record ProductArchiveYearResponseDTO(
        int year,
        long productCount,
        List<ProductArchiveQuarterResponseDTO> quarters
) {
}
