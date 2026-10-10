package com.juuls_trinkets.bar_scan.service.retention;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import com.juuls_trinkets.bar_scan.repository.ScanDataRetentionRepository;

class ScanDataRetentionServiceTests {

    private final ScanDataRetentionRepository retentionRepository = mock(ScanDataRetentionRepository.class);
    private final Clock clock = Clock.fixed(Instant.parse("2026-10-10T09:00:00Z"), ZoneOffset.UTC);
    private final ScanDataRetentionService service = new ScanDataRetentionService(retentionRepository, clock, 5);

    @Test
    void deletesScanDataOlderThanTheRetentionWindow() {
        service.deleteExpiredScanData();

        ArgumentCaptor<LocalDateTime> cutoffCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        InOrder inOrder = inOrder(retentionRepository);
        inOrder.verify(retentionRepository).deleteScannedProductsOlderThan(cutoffCaptor.capture());
        inOrder.verify(retentionRepository).deleteEmptyScanningSessionsOlderThan(cutoffCaptor.capture());

        assertThat(cutoffCaptor.getAllValues())
                .containsExactly(
                        LocalDateTime.of(2021, 10, 10, 9, 0),
                        LocalDateTime.of(2021, 10, 10, 9, 0)
                );
    }
}
