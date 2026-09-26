package com.juuls_trinkets.bar_scan.model;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

public class Product {

    private final UUID companyId;
    private final UUID shopId;
    private final UUID productId;
    private final String barcode;
    private final int shelfOfOrigin;
    private final String storeSegment;
    private final int amountCounted;
    private final LocalDateTime countedDate;

    public Product(UUID companyId, UUID productId, String barcode, int shelfOfOrigin, int amountCounted) {
        this(companyId, null, productId, barcode, shelfOfOrigin, String.valueOf(shelfOfOrigin), amountCounted, LocalDateTime.now(ZoneOffset.UTC));
    }

    public Product(UUID companyId, UUID shopId, UUID productId, String barcode, int shelfOfOrigin, String storeSegment, int amountCounted) {
        this(companyId, shopId, productId == null ? UUID.randomUUID() : productId, barcode, shelfOfOrigin, storeSegment, amountCounted, LocalDateTime.now(ZoneOffset.UTC));
    }

    public Product(UUID companyId, UUID productId, String barcode, int shelfOfOrigin, int amountCounted, LocalDateTime countedDate) {
        this(companyId, null, productId, barcode, shelfOfOrigin, String.valueOf(shelfOfOrigin), amountCounted, countedDate);
    }

    public Product(UUID companyId, UUID shopId, UUID productId, String barcode, int shelfOfOrigin, String storeSegment, int amountCounted, LocalDateTime countedDate) {
        this.companyId = companyId;
        this.shopId = shopId;
        this.productId = productId;
        this.barcode = barcode;
        this.shelfOfOrigin = shelfOfOrigin;
        this.storeSegment = storeSegment;
        this.amountCounted = amountCounted;
        this.countedDate = countedDate;
    }

    public UUID getCompanyId() {
        return companyId;
    }

    public UUID getShopId() {
        return shopId;
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

    public String getStoreSegment() {
        return storeSegment;
    }

    public int getAmountCounted() {
        return amountCounted;
    }

    public LocalDateTime getCountedDate() {
        return countedDate;
    }
}
