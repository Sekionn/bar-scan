package com.juuls_trinkets.bar_scan.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class ScanningSession {

    private final UUID id;
    private final UUID companyId;
    private final UUID shopId;
    private final UUID startedByUserId;
    private final LocalDateTime startedAt;
    private final LocalDateTime closedAt;

    public ScanningSession(
            UUID id,
            UUID companyId,
            UUID shopId,
            UUID startedByUserId,
            LocalDateTime startedAt,
            LocalDateTime closedAt
    ) {
        this.id = id;
        this.companyId = companyId;
        this.shopId = shopId;
        this.startedByUserId = startedByUserId;
        this.startedAt = startedAt;
        this.closedAt = closedAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getCompanyId() {
        return companyId;
    }

    public UUID getShopId() {
        return shopId;
    }

    public UUID getStartedByUserId() {
        return startedByUserId;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public LocalDateTime getClosedAt() {
        return closedAt;
    }

    public boolean isOpen() {
        return closedAt == null;
    }
}
