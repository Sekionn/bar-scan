package com.juuls_trinkets.bar_scan.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.juuls_trinkets.bar_scan.model.Product;

public record ProductResponseDTO(
        UUID companyId,
        UUID productId,
        String barcode,
        int shelfOfOrigin,
        int amountCounted,
        LocalDateTime countedDate
) {
    public ProductResponseDTO(Product product) {
        this(
                product.getCompanyId(),
                product.getProductId(),
                product.getBarcode(),
                product.getShelfOfOrigin(),
                product.getAmountCounted(),
                product.getCountedDate()
        );
    }
}
