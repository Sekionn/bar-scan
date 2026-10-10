package com.juuls_trinkets.bar_scan.dto;

import java.time.LocalDate;

public record ProductArchiveDateResponseDTO(
        LocalDate date,
        long productCount
) {
}
