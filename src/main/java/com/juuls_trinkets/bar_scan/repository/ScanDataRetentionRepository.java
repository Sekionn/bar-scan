package com.juuls_trinkets.bar_scan.repository;

import java.time.LocalDateTime;

public interface ScanDataRetentionRepository {

    int deleteScannedProductsOlderThan(LocalDateTime cutoff);

    int deleteEmptyScanningSessionsOlderThan(LocalDateTime cutoff);
}
