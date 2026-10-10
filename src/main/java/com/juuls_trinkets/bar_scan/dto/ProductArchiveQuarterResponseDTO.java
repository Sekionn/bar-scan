package com.juuls_trinkets.bar_scan.dto;

import java.util.List;

public record ProductArchiveQuarterResponseDTO(
        String quarter,
        long productCount,
        List<ProductArchiveDateResponseDTO> dates
) {
}
