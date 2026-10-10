package com.juuls_trinkets.bar_scan.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record ProductArchiveItemResponseDTO(
        UUID shopId,
        String shopName,
        UUID sessionId,
        String barcode,
        int shelfOfOrigin,
        String storeSegment,
        int amountCounted,
        LocalDateTime countedDate,
        int year,
        String quarter,
        LocalDate date
) {
}
