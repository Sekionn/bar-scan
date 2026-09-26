package com.juuls_trinkets.bar_scan.service;

import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import com.juuls_trinkets.bar_scan.dto.ContactSignupRequestDTO;

@Service
public class SignupNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(SignupNotificationService.class);

    private final NamedParameterJdbcTemplate template;
    private final String superAdminEmail;
    private final String notificationMode;

    public SignupNotificationService(
            NamedParameterJdbcTemplate template,
            @Value("${app.super-admin.email:}") String superAdminEmail,
            @Value("${app.signup.notification-mode:database}") String notificationMode
    ) {
        this.template = template;
        this.superAdminEmail = superAdminEmail;
        this.notificationMode = notificationMode;
    }

    public void notifyContactSignup(UUID signupId, ContactSignupRequestDTO request, String adminEmail) {
        template.update(
                """
                insert into signup_notifications(
                    id,
                    signup_id,
                    recipient_email,
                    notification_mode,
                    company_name,
                    location_name,
                    allowed_user_count,
                    admin_email,
                    message,
                    status
                )
                values(
                    :id,
                    :signupId,
                    :recipientEmail,
                    :notificationMode,
                    :companyName,
                    :locationName,
                    :allowedUserCount,
                    :adminEmail,
                    :message,
                    :status
                )
                """,
                Map.of(
                        "id", UUID.randomUUID().toString(),
                        "signupId", signupId.toString(),
                        "recipientEmail", superAdminEmail,
                        "notificationMode", notificationMode,
                        "companyName", request.getCompanyName().trim(),
                        "locationName", request.getLocationName().trim(),
                        "allowedUserCount", request.getAllowedUserCount(),
                        "adminEmail", adminEmail,
                        "message", normalizeMessage(request.getMessage()),
                        "status", "PENDING"
                )
        );

        logger.info(
                "Contact signup requested: company={}, location={}, allowedUserCount={}, adminEmail={}, message={}",
                request.getCompanyName().trim(),
                request.getLocationName().trim(),
                request.getAllowedUserCount(),
                adminEmail,
                normalizeMessage(request.getMessage())
        );
    }

    private static String normalizeMessage(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return value.trim();
    }
}
