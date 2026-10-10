package com.juuls_trinkets.bar_scan.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.juuls_trinkets.bar_scan.model.ScanningSessionProduct;

public record ProductResponseDTO(
        UUID companyId,
        UUID messageId,
        UUID shopId,
        UUID scanningSessionId,
        String barcode,
        int shelfOfOrigin,
        String storeSegment,
        int amountCounted,
        LocalDateTime countedDate,
        String countedQuarter
) {
    public ProductResponseDTO(UUID companyId, UUID shopId, ScanningSessionProduct product) {
        this(
                companyId,
                product.getMessageId(),
                shopId,
                product.getScanningSessionId(),
                product.getBarcode(),
                product.getShelfOfOrigin(),
                product.getStoreSegment(),
                product.getAmountCounted(),
                product.getCountedDate(),
                "%d-Q%d".formatted(product.getCountedDate().getYear(), ((product.getCountedDate().getMonthValue() - 1) / 3) + 1)
        );
    }
}
