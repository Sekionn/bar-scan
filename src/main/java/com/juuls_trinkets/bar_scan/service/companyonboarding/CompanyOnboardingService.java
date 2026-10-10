package com.juuls_trinkets.bar_scan.service.companyonboarding;

import java.util.Locale;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.juuls_trinkets.bar_scan.dto.CompanyResponseDTO;
import com.juuls_trinkets.bar_scan.dto.OnboardCompanyRequestDTO;
import com.juuls_trinkets.bar_scan.dto.OnboardCompanyResponseDTO;
import com.juuls_trinkets.bar_scan.dto.UserResponseDTO;
import com.juuls_trinkets.bar_scan.exception.BadRequestException;
import com.juuls_trinkets.bar_scan.exception.DuplicateResourceException;
import com.juuls_trinkets.bar_scan.exception.ResourceNotFoundException;
import com.juuls_trinkets.bar_scan.model.AppUser;
import com.juuls_trinkets.bar_scan.model.Company;
import com.juuls_trinkets.bar_scan.model.Shop;
import com.juuls_trinkets.bar_scan.repository.CompanyRepository;
import com.juuls_trinkets.bar_scan.repository.ShopRepository;
import com.juuls_trinkets.bar_scan.repository.UserRepository;
import com.juuls_trinkets.bar_scan.service.authorization.UserAccessPolicy;

@Service
public class CompanyOnboardingService implements CompanyOnboardingApplicationService {

    private static final String PAYMENT_PAID = "PAID";
    private static final String ONBOARDING_COMPLETED = "COMPLETED";

    private final NamedParameterJdbcTemplate template;
    private final CompanyRepository companyRepository;
    private final ShopRepository shopRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserAccessPolicy userAccessPolicy;

    public CompanyOnboardingService(
            NamedParameterJdbcTemplate template,
            CompanyRepository companyRepository,
            ShopRepository shopRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            UserAccessPolicy userAccessPolicy
    ) {
        this.template = template;
        this.companyRepository = companyRepository;
        this.shopRepository = shopRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userAccessPolicy = userAccessPolicy;
    }

    @Transactional
    @Override
    public OnboardCompanyResponseDTO onboardCompany(OnboardCompanyRequestDTO request) {
        String adminEmail = request.getAdminEmail().trim().toLowerCase(Locale.ROOT);

        userRepository.findByUsername(adminEmail).ifPresent(user -> {
            throw new DuplicateResourceException("Admin email already exists");
        });

        Company company = companyRepository.create(new Company(
                UUID.randomUUID(),
                request.getCompanyName().trim(),
                request.getAllowedUserCount(),
                request.getLocationName().trim()
        ));

        Shop shop = shopRepository.create(new Shop(
                UUID.randomUUID(),
                company.getId(),
                request.getLocationName().trim()
        ));

        AppUser admin = userRepository.create(new AppUser(
                UUID.randomUUID(),
                adminEmail,
                passwordEncoder.encode(request.getAdminPassword()),
                company.getId(),
                null,
                "ROLE_ADMIN"
        ));

        return new OnboardCompanyResponseDTO(
                new CompanyResponseDTO(company),
                new UserResponseDTO(admin, userAccessPolicy)
        );
    }

    @Transactional
    @Override
    public OnboardCompanyResponseDTO onboardPaidSignup(UUID signupId) {
        SignupRequestRow signup = findSignupForUpdate(signupId);

        if (ONBOARDING_COMPLETED.equals(signup.onboardingStatus())) {
            Company company = companyRepository.findById(signup.onboardedCompanyId())
                    .orElseThrow(() -> new ResourceNotFoundException("Onboarded company not found"));
            AppUser admin = userRepository.findById(signup.onboardedAdminUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("Onboarded admin user not found"));
            return new OnboardCompanyResponseDTO(new CompanyResponseDTO(company), new UserResponseDTO(admin, userAccessPolicy));
        }

        if (!PAYMENT_PAID.equals(signup.paymentStatus())) {
            throw new BadRequestException("Signup payment has not been completed");
        }

        userRepository.findByUsername(signup.adminEmail()).ifPresent(user -> {
            throw new DuplicateResourceException("Admin email already exists");
        });

        Company company = companyRepository.create(new Company(
                UUID.randomUUID(),
                signup.companyName(),
                signup.allowedUserCount(),
                signup.locationName()
        ));

        Shop shop = shopRepository.create(new Shop(
                UUID.randomUUID(),
                company.getId(),
                signup.locationName()
        ));

        AppUser admin = userRepository.create(new AppUser(
                UUID.randomUUID(),
                signup.adminEmail(),
                signup.adminPasswordHash(),
                company.getId(),
                null,
                "ROLE_ADMIN"
        ));

        template.update(
                """
                update signup_requests
                set onboarding_status = :onboardingStatus,
                    onboarded_company_id = :companyId,
                    onboarded_admin_user_id = :adminUserId
                where id = :id
                """,
                Map.of(
                        "id", signup.id().toString(),
                        "onboardingStatus", ONBOARDING_COMPLETED,
                        "companyId", company.getId().toString(),
                        "adminUserId", admin.getId().toString()
                )
        );

        return new OnboardCompanyResponseDTO(
                new CompanyResponseDTO(company),
                new UserResponseDTO(admin, userAccessPolicy)
        );
    }

    private SignupRequestRow findSignupForUpdate(UUID signupId) {
        List<SignupRequestRow> rows = template.query(
                "select * from signup_requests where id = :id for update",
                Map.of("id", signupId.toString()),
                (result, rowNumber) -> new SignupRequestRow(
                        UUID.fromString(result.getString("id")),
                        result.getString("company_name"),
                        result.getString("location_name"),
                        result.getInt("allowed_user_count"),
                        result.getString("admin_email"),
                        result.getString("admin_password_hash"),
                        result.getString("payment_status"),
                        result.getString("onboarding_status"),
                        nullableUuid(result.getString("onboarded_company_id")),
                        nullableUuid(result.getString("onboarded_admin_user_id"))
                )
        );
        return rows.stream()
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Signup request not found"));
    }

    private static UUID nullableUuid(String value) {
        return value == null ? null : UUID.fromString(value);
    }

    private record SignupRequestRow(
            UUID id,
            String companyName,
            String locationName,
            int allowedUserCount,
            String adminEmail,
            String adminPasswordHash,
            String paymentStatus,
            String onboardingStatus,
            UUID onboardedCompanyId,
            UUID onboardedAdminUserId
    ) {
    }
}
