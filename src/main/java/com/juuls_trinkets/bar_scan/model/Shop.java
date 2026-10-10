package com.juuls_trinkets.bar_scan.model;

import java.util.UUID;

public class Shop {

    private final UUID id;
    private final UUID companyId;
    private final String name;
    private final boolean active;

    public Shop(UUID id, UUID companyId, String name) {
        this(id, companyId, name, true);
    }

    public Shop(UUID id, UUID companyId, String name, boolean active) {
        this.id = id;
        this.companyId = companyId;
        this.name = name;
        this.active = active;
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

    public boolean isActive() {
        return active;
    }
}
