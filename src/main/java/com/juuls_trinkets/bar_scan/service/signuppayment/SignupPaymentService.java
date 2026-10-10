package com.juuls_trinkets.bar_scan.service.signuppayment;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.juuls_trinkets.bar_scan.dto.PaymentCompleteResponseDTO;
import com.juuls_trinkets.bar_scan.dto.SignupCheckoutResponseDTO;
import com.juuls_trinkets.bar_scan.dto.StartSignupRequestDTO;
import com.juuls_trinkets.bar_scan.exception.DuplicateResourceException;
import com.juuls_trinkets.bar_scan.exception.ResourceNotFoundException;
import com.juuls_trinkets.bar_scan.repository.UserRepository;
import com.juuls_trinkets.bar_scan.service.companyonboarding.CompanyOnboardingApplicationService;

@Service
public class SignupPaymentService implements SignupPaymentApplicationService {

    private static final String PAYMENT_PENDING = "PAYMENT_PENDING";
    private static final String PAYMENT_PAID = "PAID";
    private static final String ONBOARDING_PENDING = "PENDING";

    private final NamedParameterJdbcTemplate template;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CompanyOnboardingApplicationService companyOnboardingService;
    private final String devCheckoutBaseUrl;

    public SignupPaymentService(
            NamedParameterJdbcTemplate template,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            CompanyOnboardingApplicationService companyOnboardingService,
            @Value("${app.payments.dev-checkout-base-url}") String devCheckoutBaseUrl
    ) {
        this.template = template;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.companyOnboardingService = companyOnboardingService;
        this.devCheckoutBaseUrl = devCheckoutBaseUrl;
    }

    @Transactional
    @Override
    public SignupCheckoutResponseDTO startSignup(StartSignupRequestDTO request) {
        String adminEmail = request.getAdminEmail().trim().toLowerCase(Locale.ROOT);
        userRepository.findByUsername(adminEmail).ifPresent(user -> {
            throw new DuplicateResourceException("Admin email already exists");
        });

        UUID signupId = UUID.randomUUID();
        UUID paymentSessionId = UUID.randomUUID();

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
                        payment_session_id
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
                        :paymentSessionId
                    )
                    """,
                    new MapSqlParameterSource()
                            .addValue("id", signupId.toString())
                            .addValue("companyName", request.getCompanyName().trim())
                            .addValue("locationName", null)
                            .addValue("allowedUserCount", request.getAllowedUserCount())
                            .addValue("adminEmail", adminEmail)
                            .addValue("adminPasswordHash", passwordEncoder.encode(request.getAdminPassword()))
                            .addValue("paymentStatus", PAYMENT_PENDING)
                            .addValue("onboardingStatus", ONBOARDING_PENDING)
                            .addValue("paymentSessionId", paymentSessionId.toString())
            );
        } catch (DuplicateKeyException ex) {
            throw new DuplicateResourceException("Admin email already exists");
        }

        return new SignupCheckoutResponseDTO(
                signupId,
                paymentSessionId,
                devCheckoutBaseUrl + "/" + paymentSessionId,
                PAYMENT_PENDING,
                ONBOARDING_PENDING
        );
    }

    @Override
    public SignupCheckoutResponseDTO findSignup(UUID signupId) {
        SignupRequestRow signup = findById(signupId);
        return new SignupCheckoutResponseDTO(
                signup.id(),
                signup.paymentSessionId(),
                devCheckoutBaseUrl + "/" + signup.paymentSessionId(),
                signup.paymentStatus(),
                signup.onboardingStatus()
        );
    }

    @Transactional
    @Override
    public PaymentCompleteResponseDTO completeDevPayment(UUID paymentSessionId) {
        SignupRequestRow signup = findByPaymentSessionId(paymentSessionId);

        if (!PAYMENT_PAID.equals(signup.paymentStatus())) {
            template.update(
                    """
                    update signup_requests
                    set payment_status = :paymentStatus
                    where id = :id
                    """,
                    Map.of(
                            "id", signup.id().toString(),
                            "paymentStatus", PAYMENT_PAID
                    )
            );
        }

        companyOnboardingService.onboardPaidSignup(signup.id());

        return new PaymentCompleteResponseDTO(
                signup.id(),
                signup.paymentSessionId(),
                PAYMENT_PAID,
                "COMPLETED"
        );
    }

    private SignupRequestRow findById(UUID signupId) {
        return queryOne(
                "select * from signup_requests where id = :id",
                Map.of("id", signupId.toString()),
                "Signup request not found"
        );
    }

    private SignupRequestRow findByPaymentSessionId(UUID paymentSessionId) {
        return queryOne(
                "select * from signup_requests where payment_session_id = :paymentSessionId",
                Map.of("paymentSessionId", paymentSessionId.toString()),
                "Payment session not found"
        );
    }

    private SignupRequestRow queryOne(String sql, Map<String, String> params, String notFoundMessage) {
        List<SignupRequestRow> rows = template.query(
                sql,
                new MapSqlParameterSource(params),
                (result, rowNumber) -> new SignupRequestRow(
                        UUID.fromString(result.getString("id")),
                        UUID.fromString(result.getString("payment_session_id")),
                        result.getString("payment_status"),
                        result.getString("onboarding_status")
                )
        );
        return rows.stream()
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(notFoundMessage));
    }

    private record SignupRequestRow(
            UUID id,
            UUID paymentSessionId,
            String paymentStatus,
            String onboardingStatus
    ) {
    }
}
