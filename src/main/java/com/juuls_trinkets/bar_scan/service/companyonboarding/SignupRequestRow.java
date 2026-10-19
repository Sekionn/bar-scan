package com.juuls_trinkets.bar_scan.service.companyonboarding;

import java.util.UUID;

record SignupRequestRow(
        UUID id,
        String companyName,
        int allowedUserCount,
        String adminEmail,
        String adminPasswordHash,
        String paymentStatus,
        String onboardingStatus,
        UUID onboardedCompanyId,
        UUID onboardedAdminUserId
) {
}
