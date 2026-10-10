package com.juuls_trinkets.bar_scan.service;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.juuls_trinkets.bar_scan.dto.ContactSignupRequestDTO;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.MimeMessageHelper;

@Service
public class SignupNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(SignupNotificationService.class);

    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final String superAdminEmail;
    private final String fromEmail;
    private final String toEmail;

    public SignupNotificationService(
            ObjectProvider<JavaMailSender> mailSenderProvider,
            @Value("${app.super-admin.email:}") String superAdminEmail,
            @Value("${app.signup.email.from:support.bar-scan@juuls-trinkets.com}") String fromEmail,
            @Value("${app.signup.email.to:support.bar-scan@juuls-trinkets.com}") String toEmail
    ) {
        this.mailSenderProvider = mailSenderProvider;
        this.superAdminEmail = superAdminEmail;
        this.fromEmail = fromEmail;
        this.toEmail = toEmail;
    }

    public void notifyContactSignup(UUID signupId, ContactSignupRequestDTO request, String adminEmail) {
        sendSmtpNotification(signupId, request, adminEmail);

        logger.info(
                "Contact signup requested: company={}, location={}, allowedUserCount={}, adminEmail={}, message={}",
                request.getCompanyName().trim(),
                request.getLocationName().trim(),
                request.getAllowedUserCount(),
                adminEmail,
                normalizeMessage(request.getMessage())
        );
    }

    private void sendSmtpNotification(UUID signupId, ContactSignupRequestDTO request, String adminEmail) {
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        String recipientEmail = resolvedRecipientEmail();

        if (mailSender == null || fromEmail.isBlank() || recipientEmail.isBlank()) {
            logger.warn("Signup notification email skipped because SMTP configuration is incomplete");
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(recipientEmail);
            helper.setSubject("New Bar'scan signup request");
            helper.setText(
                    buildPlainTextBody(signupId, request, adminEmail),
                    buildHtmlBody(signupId, request, adminEmail)
            );
            mailSender.send(message);
        } catch (MailException | MessagingException ex) {
            logger.warn("Signup notification email could not be sent through SMTP", ex);
        }
    }

    private String resolvedRecipientEmail() {
        if (!toEmail.isBlank()) {
            return toEmail;
        }
        return superAdminEmail;
    }

    private static String buildPlainTextBody(UUID signupId, ContactSignupRequestDTO request, String adminEmail) {
        return """
                New Bar'scan company signup request

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
                <h2>New Bar&#39;scan company signup request</h2>
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

}
