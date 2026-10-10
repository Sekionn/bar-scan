package com.juuls_trinkets.bar_scan.dto;

import java.util.UUID;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SignupNotificationEventDTO(
        @NotNull UUID signupId,
        @NotBlank String companyName,
        @NotBlank String locationName,
        @Min(1) int allowedUserCount,
        @Email @NotBlank String adminEmail,
        String message
) {
}
