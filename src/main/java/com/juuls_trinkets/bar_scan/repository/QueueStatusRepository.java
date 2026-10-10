package com.juuls_trinkets.bar_scan.repository;

import java.util.List;
import java.util.UUID;

import com.juuls_trinkets.bar_scan.dto.QueueStatusResponseDTO;

public interface QueueStatusRepository {

    int registerExpected(UUID messageId, UUID syncBatchId, UUID companyId, UUID shopId);

    void cancelUnpublished(UUID messageId, UUID syncBatchId, UUID companyId, UUID shopId);

    void markProcessed(UUID messageId, UUID syncBatchId, UUID companyId, UUID shopId);

    boolean isProcessed(UUID messageId);

    QueueStatusResponseDTO findStatus(UUID companyId, UUID shopId);

    List<QueueStatusResponseDTO> findStatuses(UUID companyId);
}
