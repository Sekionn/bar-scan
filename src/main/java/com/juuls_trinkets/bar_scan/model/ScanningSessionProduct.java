package com.juuls_trinkets.bar_scan.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class ScanningSessionProduct {

    private final UUID scanningSessionId;
    private final UUID messageId;
    private final String barcode;
    private final int shelfOfOrigin;
    private final String storeSegment;
    private final int amountCounted;
    private final LocalDateTime countedDate;

    public ScanningSessionProduct(
            UUID scanningSessionId,
            UUID messageId,
            String barcode,
            int shelfOfOrigin,
            String storeSegment,
            int amountCounted,
            LocalDateTime countedDate
    ) {
        this.scanningSessionId = scanningSessionId;
        this.messageId = messageId;
        this.barcode = barcode;
        this.shelfOfOrigin = shelfOfOrigin;
        this.storeSegment = storeSegment;
        this.amountCounted = amountCounted;
        this.countedDate = countedDate;
    }

    public UUID getScanningSessionId() {
        return scanningSessionId;
    }

    public UUID getMessageId() {
        return messageId;
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
