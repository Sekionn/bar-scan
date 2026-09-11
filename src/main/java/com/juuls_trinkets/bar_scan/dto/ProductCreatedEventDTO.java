package com.juuls_trinkets.bar_scan.dto;

import java.util.UUID;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProductCreatedEventDTO(
        @NotNull
        UUID userId,

        UUID productId,

        @NotBlank
        String barcode,

        @Min(0)
        int shelfOfOrigin,

        int amountCounted
) {
}
