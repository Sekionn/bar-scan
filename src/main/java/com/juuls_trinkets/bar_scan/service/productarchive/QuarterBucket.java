package com.juuls_trinkets.bar_scan.service.productarchive;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.juuls_trinkets.bar_scan.dto.ProductArchiveDateResponseDTO;
import com.juuls_trinkets.bar_scan.dto.ProductArchiveQuarterResponseDTO;

class QuarterBucket {
    private final String quarter;
    final List<ProductArchiveDateResponseDTO> dates = new ArrayList<>();
    long productCount;

    QuarterBucket(String quarter) {
        this.quarter = quarter;
    }

    int quarterNumber() {
        return Integer.parseInt(quarter.replace("Q", ""));
    }

    ProductArchiveQuarterResponseDTO toResponse() {
        return new ProductArchiveQuarterResponseDTO(
                quarter,
                productCount,
                dates.stream()
                        .sorted(Comparator.comparing(ProductArchiveDateResponseDTO::date).reversed())
                        .toList()
        );
    }
}
