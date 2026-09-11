package com.juuls_trinkets.bar_scan.model;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

public class Product {

    private final UUID companyId;
    private final UUID productId;
    private final String barcode;
    private final int shelfOfOrigin;
    private final int amountCounted;
    private final LocalDateTime countedDate;

    public Product(UUID companyId, UUID productId, String barcode, int shelfOfOrigin, int amountCounted) {
        this(companyId, productId == null ? UUID.randomUUID() : productId, barcode, shelfOfOrigin, amountCounted, LocalDateTime.now(ZoneOffset.UTC));
    }

    public Product(UUID companyId, UUID productId, String barcode, int shelfOfOrigin, int amountCounted, LocalDateTime countedDate) {
        this.companyId = companyId;
        this.productId = productId;
        this.barcode = barcode;
        this.shelfOfOrigin = shelfOfOrigin;
        this.amountCounted = amountCounted;
        this.countedDate = countedDate;
    }

    public UUID getCompanyId() {
        return companyId;
    }

    public UUID getProductId() {
        return productId;
    }

    public String getBarcode() {
        return barcode;
    }

    public int getShelfOfOrigin() {
        return shelfOfOrigin;
    }

    public int getAmountCounted() {
        return amountCounted;
    }

    public LocalDateTime getCountedDate() {
        return countedDate;
    }
}
