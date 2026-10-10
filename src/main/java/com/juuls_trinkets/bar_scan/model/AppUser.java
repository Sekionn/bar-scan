package com.juuls_trinkets.bar_scan.model;

import java.util.UUID;

public class AppUser {

    private final UUID id;
    private final String username;
    private final String passwordHash;
    private final UUID companyId;
    private final UUID shopId;
    private final String role;
    private final boolean active;

    public AppUser(UUID id, String username, String passwordHash, UUID companyId, String role) {
        this(id, username, passwordHash, companyId, null, role);
    }

    public AppUser(UUID id, String username, String passwordHash, UUID companyId, UUID shopId, String role) {
        this(id, username, passwordHash, companyId, shopId, role, true);
    }

    public AppUser(UUID id, String username, String passwordHash, UUID companyId, UUID shopId, String role, boolean active) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.companyId = companyId;
        this.shopId = shopId;
        this.role = role;
        this.active = active;
    }

    public UUID getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public UUID getCompanyId() {
        return companyId;
    }

    public UUID getShopId() {
        return shopId;
    }

    public String getRole() {
        return role;
    }

    public boolean isActive() {
        return active;
    }
}
