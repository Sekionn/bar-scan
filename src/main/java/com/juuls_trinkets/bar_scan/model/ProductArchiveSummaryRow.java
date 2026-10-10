package com.juuls_trinkets.bar_scan.model;

import java.time.LocalDate;
import java.util.UUID;

public record ProductArchiveSummaryRow(
        UUID shopId,
        String shopName,
        int year,
        int quarterNumber,
        LocalDate date,
        long productCount
) {
    public String quarter() {
        return "Q" + quarterNumber;
    }
}
