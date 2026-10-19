package com.juuls_trinkets.bar_scan.service.signuppayment;

import java.util.UUID;

record SignupRequestRow(
        UUID id,
        UUID paymentSessionId,
        String paymentStatus,
        String onboardingStatus
) {
}
