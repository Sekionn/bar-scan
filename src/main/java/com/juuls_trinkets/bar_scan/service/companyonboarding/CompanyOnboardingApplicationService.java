package com.juuls_trinkets.bar_scan.service.companyonboarding;

import java.util.UUID;

import com.juuls_trinkets.bar_scan.dto.OnboardCompanyRequestDTO;
import com.juuls_trinkets.bar_scan.dto.OnboardCompanyResponseDTO;

public interface CompanyOnboardingApplicationService {

    OnboardCompanyResponseDTO onboardCompany(OnboardCompanyRequestDTO request);

    OnboardCompanyResponseDTO onboardPaidSignup(UUID signupId);
}
