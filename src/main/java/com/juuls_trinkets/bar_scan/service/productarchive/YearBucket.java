package com.juuls_trinkets.bar_scan.service.productarchive;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;

import com.juuls_trinkets.bar_scan.dto.ProductArchiveYearResponseDTO;

class YearBucket {
    private final int year;
    final Map<String, QuarterBucket> quarters = new LinkedHashMap<>();
    long productCount;

    YearBucket(int year) {
        this.year = year;
    }

    int year() {
        return year;
    }

    ProductArchiveYearResponseDTO toResponse() {
        return new ProductArchiveYearResponseDTO(
                year,
                productCount,
                quarters.values().stream()
                        .sorted(Comparator.comparing(QuarterBucket::quarterNumber))
                        .map(QuarterBucket::toResponse)
                        .toList()
        );
    }
}
