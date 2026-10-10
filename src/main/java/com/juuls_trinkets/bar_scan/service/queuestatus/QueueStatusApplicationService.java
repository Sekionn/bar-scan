package com.juuls_trinkets.bar_scan.service.queuestatus;

import java.util.List;
import java.util.UUID;

import com.juuls_trinkets.bar_scan.dto.QueueStatusResponseDTO;
import com.juuls_trinkets.bar_scan.security.CustomUserDetails;

public interface QueueStatusApplicationService {

    void registerExpectedMessages(UUID userId, UUID syncBatchId, List<UUID> messageIds);

    void cancelUnpublishedMessages(UUID userId, UUID syncBatchId, List<UUID> messageIds);

    QueueStatusResponseDTO getStatus(CustomUserDetails user, UUID shopId);

    List<QueueStatusResponseDTO> getVisibleStatuses(CustomUserDetails user);

    boolean isProcessed(UUID messageId);

    void markProcessed(UUID messageId, UUID syncBatchId, UUID companyId, UUID shopId);
}
