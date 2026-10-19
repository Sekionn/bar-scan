package com.juuls_trinkets.bar_scan.exception;

public class UnassignedShopMessageException extends RuntimeException {

    public UnassignedShopMessageException() {
        super("User is not assigned to a shop");
    }
}
