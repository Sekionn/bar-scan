package com.juuls_trinkets.bar_scan.dto;

import java.util.UUID;

public record QueueStatusResponseDTO(
        UUID companyId,
        UUID shopId,
        long pendingMessages,
        long processedMessages,
        int pendingPercentage,
        boolean upToDate
) {
}
