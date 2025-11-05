package com.juuls_trinkets.bar_scan.models;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

public class Product {
    public UUID Id;
    public String Barcode;
    public int ShelfOfOrigin;
    public int AmountCounted;
    public LocalDateTime CountedDate;

    public Product(String barcode, int shelfOfOrigin, int amountCounted){
        Id = UUID.randomUUID();
        Barcode = barcode;
        ShelfOfOrigin = shelfOfOrigin;
        AmountCounted = amountCounted;
        CountedDate = LocalDateTime.now(ZoneOffset.UTC);
    }

    public Product(String barcode, int shelfOfOrigin, int amountCounted, LocalDateTime countedDate, UUID id)
    {
        Id = id;
        Barcode = barcode;
        ShelfOfOrigin = shelfOfOrigin;
        AmountCounted = amountCounted;
        CountedDate = countedDate;
    }
}
