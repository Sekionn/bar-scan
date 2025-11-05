package com.juuls_trinkets.bar_scan.models;

public class ProductCreationDTO {
    public String barcode;
    public int shelfOfOrigin;
    public int amountCounted;

    
    public Product createProduct(){
        return new Product(barcode, shelfOfOrigin, amountCounted);
    }
}
