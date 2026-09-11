package com.juuls_trinkets.bar_scan.model;

import java.util.UUID;

public class Company {

    private final UUID id;
    private final String name;
    private final int allowedUserCount;
    private final String locationName;

    public Company(UUID id, String name, int allowedUserCount, String locationName) {
        this.id = id;
        this.name = name;
        this.allowedUserCount = allowedUserCount;
        this.locationName = locationName;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getAllowedUserCount() {
        return allowedUserCount;
    }

    public String getLocationName() {
        return locationName;
    }
}
