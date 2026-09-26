package com.juuls_trinkets.bar_scan.dto;

import java.util.UUID;

public record SignupCheckoutResponseDTO(
        UUID signupId,
        UUID paymentSessionId,
        String paymentUrl,
        String paymentStatus,
        String onboardingStatus
) {
}
