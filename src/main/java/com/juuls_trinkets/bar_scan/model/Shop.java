package com.juuls_trinkets.bar_scan.model;

import java.util.UUID;

public class Shop {

    private final UUID id;
    private final UUID companyId;
    private final String name;

    public Shop(UUID id, UUID companyId, String name) {
        this.id = id;
        this.companyId = companyId;
        this.name = name;
    }

    public UUID getId() {
        return id;
    }

    public UUID getCompanyId() {
        return companyId;
    }

    public String getName() {
        return name;
    }
}
