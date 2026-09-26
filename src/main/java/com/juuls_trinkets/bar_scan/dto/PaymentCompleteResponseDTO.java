package com.juuls_trinkets.bar_scan.dto;

import java.util.UUID;

public record PaymentCompleteResponseDTO(
        UUID signupId,
        UUID paymentSessionId,
        String paymentStatus,
        String onboardingStatus
) {
}
