package com.juuls_trinkets.bar_scan.dto;

import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SyncBatchPublishCompletedEventDTO(
        @NotNull
        UUID messageId,

        @NotNull
        UUID syncBatchId,

        @NotNull
        UUID userId,

        @NotEmpty
        @Size(max = 1000)
        List<@NotNull UUID> unpublishedMessageIds
) {
}
