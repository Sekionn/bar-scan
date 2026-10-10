package com.juuls_trinkets.bar_scan.service.contactsignup;

import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.juuls_trinkets.bar_scan.dto.ContactSignupRequestDTO;
import com.juuls_trinkets.bar_scan.dto.ContactSignupResponseDTO;
import com.juuls_trinkets.bar_scan.exception.DuplicateResourceException;
import com.juuls_trinkets.bar_scan.model.AppUser;
import com.juuls_trinkets.bar_scan.model.Company;
import com.juuls_trinkets.bar_scan.repository.CompanyRepository;
import com.juuls_trinkets.bar_scan.repository.UserRepository;
import com.juuls_trinkets.bar_scan.service.SignupNotificationPublisher;

@Service
public class ContactSignupService implements ContactSignupApplicationService {

    private static final String CONTACT_REQUESTED = "CONTACT_REQUESTED";
    private static final String MANUAL_PAYMENT_REQUIRED = "MANUAL_PAYMENT_REQUIRED";
    private static final String ONBOARDING_COMPLETED = "COMPLETED";

    private final NamedParameterJdbcTemplate template;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SignupNotificationPublisher signupNotificationPublisher;

    public ContactSignupService(
            NamedParameterJdbcTemplate template,
            CompanyRepository companyRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            SignupNotificationPublisher signupNotificationPublisher
    ) {
        this.template = template;
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.signupNotificationPublisher = signupNotificationPublisher;
    }

    @Transactional
    @Override
    public ContactSignupResponseDTO requestContactSignup(ContactSignupRequestDTO request) {
        String adminEmail = request.getAdminEmail().trim().toLowerCase(Locale.ROOT);

        userRepository.findByUsername(adminEmail).ifPresent(user -> {
            throw new DuplicateResourceException("Admin email already exists");
        });

        UUID signupId = UUID.randomUUID();
        UUID paymentSessionId = UUID.randomUUID();
        String passwordHash = passwordEncoder.encode(UUID.randomUUID().toString());

        try {
            template.update(
                    """
                    insert into signup_requests(
                        id,
                        company_name,
                        location_name,
                        allowed_user_count,
                        admin_email,
                        admin_password_hash,
                        payment_status,
                        onboarding_status,
                        payment_session_id,
                        contact_status,
                        subscription_status,
                        message
                    )
                    values(
                        :id,
                        :companyName,
                        :locationName,
                        :allowedUserCount,
                        :adminEmail,
                        :adminPasswordHash,
                        :paymentStatus,
                        :onboardingStatus,
                        :paymentSessionId,
                        :contactStatus,
                        :subscriptionStatus,
                        :message
                    )
                    """,
                    new MapSqlParameterSource()
                            .addValue("id", signupId.toString())
                            .addValue("companyName", request.getCompanyName().trim())
                            .addValue("locationName", null)
                            .addValue("allowedUserCount", request.getAllowedUserCount())
                            .addValue("adminEmail", adminEmail)
                            .addValue("adminPasswordHash", passwordHash)
                            .addValue("paymentStatus", MANUAL_PAYMENT_REQUIRED)
                            .addValue("onboardingStatus", ONBOARDING_COMPLETED)
                            .addValue("paymentSessionId", paymentSessionId.toString())
                            .addValue("contactStatus", CONTACT_REQUESTED)
                            .addValue("subscriptionStatus", MANUAL_PAYMENT_REQUIRED)
                            .addValue("message", normalizeMessage(request.getMessage()))
            );
        } catch (DuplicateKeyException ex) {
            throw new DuplicateResourceException("Admin email already exists");
        }

        Company company = companyRepository.create(new Company(
                UUID.randomUUID(),
                request.getCompanyName().trim(),
                request.getAllowedUserCount(),
                request.getCompanyName().trim()
        ));

        AppUser admin = userRepository.create(new AppUser(
                UUID.randomUUID(),
                adminEmail,
                passwordHash,
                company.getId(),
                null,
                "ROLE_ADMIN"
        ));

        template.update(
                """
                update signup_requests
                set onboarded_company_id = :companyId,
                    onboarded_admin_user_id = :adminUserId
                where id = :id
                """,
                Map.of(
                        "id", signupId.toString(),
                        "companyId", company.getId().toString(),
                        "adminUserId", admin.getId().toString()
                )
        );

        signupNotificationPublisher.publishAfterCommit(signupId, request, adminEmail);

        return new ContactSignupResponseDTO(
                signupId,
                company.getId(),
                admin.getId(),
                admin.getUsername(),
                CONTACT_REQUESTED,
                "Signup received. We will contact the admin email."
        );
    }

    private static String normalizeMessage(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return value.trim();
    }
}
