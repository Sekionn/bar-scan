package com.juuls_trinkets.bar_scan.dto;

import java.util.UUID;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProductCreatedEventDTO(
        @NotNull
        UUID messageId,

        @NotNull
        UUID syncBatchId,

        @NotNull
        UUID userId,

        @NotBlank
        String barcode,

        @Min(0)
        int shelfOfOrigin,

        @NotBlank
        String storeSegment,

        @Min(0)
        int amountCounted
) {
}
