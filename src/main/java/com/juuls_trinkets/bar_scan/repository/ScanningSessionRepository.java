package com.juuls_trinkets.bar_scan.repository;

import java.util.Optional;
import java.util.UUID;

import com.juuls_trinkets.bar_scan.model.ScanningSession;
import com.juuls_trinkets.bar_scan.model.ScanningSessionProduct;

public interface ScanningSessionRepository {

    ScanningSession create(ScanningSession session);

    Optional<ScanningSession> findOpenByShopId(UUID shopId);

    ScanningSessionProduct addProductScan(ScanningSessionProduct product);
}
