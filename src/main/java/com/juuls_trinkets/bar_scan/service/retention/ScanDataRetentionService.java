package com.juuls_trinkets.bar_scan.service.retention;

import java.time.Clock;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.juuls_trinkets.bar_scan.repository.ScanDataRetentionRepository;

@Service
public class ScanDataRetentionService {

    private final ScanDataRetentionRepository retentionRepository;
    private final Clock clock;
    private final int retentionYears;

    @Autowired
    public ScanDataRetentionService(
            ScanDataRetentionRepository retentionRepository,
            @Value("${app.retention.scan-data-years:5}") int retentionYears
    ) {
        this(retentionRepository, Clock.systemDefaultZone(), retentionYears);
    }

    ScanDataRetentionService(
            ScanDataRetentionRepository retentionRepository,
            Clock clock,
            int retentionYears
    ) {
        this.retentionRepository = retentionRepository;
        this.clock = clock;
        this.retentionYears = retentionYears;
    }

    @Scheduled(cron = "${app.retention.scan-data-cleanup-cron:0 30 3 * * *}")
    @Transactional
    public void deleteExpiredScanData() {
        LocalDateTime cutoff = LocalDateTime.now(clock).minusYears(retentionYears);
        retentionRepository.deleteScannedProductsOlderThan(cutoff);
        retentionRepository.deleteEmptyScanningSessionsOlderThan(cutoff);
    }
}
