package com.juuls_trinkets.bar_scan.service;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.juuls_trinkets.bar_scan.dto.ContactSignupRequestDTO;

@Service
public class SignupNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(SignupNotificationService.class);
    private static final String RESEND_MODE = "resend";
    private static final String RESEND_EMAILS_URL = "https://api.resend.com/emails";

    private final RestClient restClient;
    private final String superAdminEmail;
    private final String notificationMode;
    private final String resendApiKey;
    private final String resendFromEmail;
    private final String resendToEmail;

    public SignupNotificationService(
            RestClient.Builder restClientBuilder,
            @Value("${app.super-admin.email:}") String superAdminEmail,
            @Value("${app.signup.notification-mode:log}") String notificationMode,
            @Value("${app.signup.resend.api-key:}") String resendApiKey,
            @Value("${app.signup.resend.from:}") String resendFromEmail,
            @Value("${app.signup.resend.to:}") String resendToEmail
    ) {
        this.restClient = restClientBuilder.build();
        this.superAdminEmail = superAdminEmail;
        this.notificationMode = notificationMode;
        this.resendApiKey = resendApiKey;
        this.resendFromEmail = resendFromEmail;
        this.resendToEmail = resendToEmail;
    }

    public void notifyContactSignup(UUID signupId, ContactSignupRequestDTO request, String adminEmail) {
        if (RESEND_MODE.equalsIgnoreCase(notificationMode)) {
            sendResendNotification(signupId, request, adminEmail);
        }

        logger.info(
                "Contact signup requested: company={}, location={}, allowedUserCount={}, adminEmail={}, message={}",
                request.getCompanyName().trim(),
                request.getLocationName().trim(),
                request.getAllowedUserCount(),
                adminEmail,
                normalizeMessage(request.getMessage())
        );
    }

    private void sendResendNotification(UUID signupId, ContactSignupRequestDTO request, String adminEmail) {
        String recipientEmail = resolvedRecipientEmail();

        if (resendApiKey.isBlank() || resendFromEmail.isBlank() || recipientEmail.isBlank()) {
            logger.warn("Signup notification email skipped because Resend configuration is incomplete");
            return;
        }

        try {
            restClient.post()
                    .uri(RESEND_EMAILS_URL)
                    .header("Authorization", "Bearer " + resendApiKey)
                    .body(new ResendEmailRequest(
                            resendFromEmail,
                            List.of(recipientEmail),
                            "New Bar Scan signup request",
                            buildPlainTextBody(signupId, request, adminEmail),
                            buildHtmlBody(signupId, request, adminEmail)
                    ))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException ex) {
            logger.warn("Signup notification email could not be sent through Resend", ex);
        }
    }

    private String resolvedRecipientEmail() {
        if (!resendToEmail.isBlank()) {
            return resendToEmail;
        }
        return superAdminEmail;
    }

    private static String buildPlainTextBody(UUID signupId, ContactSignupRequestDTO request, String adminEmail) {
        return """
                New company signup request

                Signup ID: %s
                Company: %s
                Location: %s
                Allowed users: %d
                Admin email: %s
                Message: %s
                """.formatted(
                signupId,
                request.getCompanyName().trim(),
                request.getLocationName().trim(),
                request.getAllowedUserCount(),
                adminEmail,
                normalizeMessage(request.getMessage())
        );
    }

    private static String buildHtmlBody(UUID signupId, ContactSignupRequestDTO request, String adminEmail) {
        return """
                <h2>New company signup request</h2>
                <dl>
                  <dt>Signup ID</dt><dd>%s</dd>
                  <dt>Company</dt><dd>%s</dd>
                  <dt>Location</dt><dd>%s</dd>
                  <dt>Allowed users</dt><dd>%d</dd>
                  <dt>Admin email</dt><dd>%s</dd>
                  <dt>Message</dt><dd>%s</dd>
                </dl>
                """.formatted(
                escapeHtml(signupId.toString()),
                escapeHtml(request.getCompanyName().trim()),
                escapeHtml(request.getLocationName().trim()),
                request.getAllowedUserCount(),
                escapeHtml(adminEmail),
                escapeHtml(normalizeMessage(request.getMessage()))
        );
    }

    private static String normalizeMessage(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return value.trim();
    }

    private static String escapeHtml(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private record ResendEmailRequest(
            String from,
            List<String> to,
            String subject,
            String text,
            String html
    ) {
    }
}
