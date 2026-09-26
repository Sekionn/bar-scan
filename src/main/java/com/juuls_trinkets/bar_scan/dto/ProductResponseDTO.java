package com.juuls_trinkets.bar_scan.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.juuls_trinkets.bar_scan.model.Product;

public record ProductResponseDTO(
        UUID companyId,
        UUID shopId,
        UUID productId,
        String barcode,
        int shelfOfOrigin,
        String storeSegment,
        int amountCounted,
        LocalDateTime countedDate,
        String countedQuarter
) {
    public ProductResponseDTO(Product product) {
        this(
                product.getCompanyId(),
                product.getShopId(),
                product.getProductId(),
                product.getBarcode(),
                product.getShelfOfOrigin(),
                product.getStoreSegment(),
                product.getAmountCounted(),
                product.getCountedDate(),
                "%d-Q%d".formatted(product.getCountedDate().getYear(), ((product.getCountedDate().getMonthValue() - 1) / 3) + 1)
        );
    }
}
