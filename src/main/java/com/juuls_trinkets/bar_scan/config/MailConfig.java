package com.juuls_trinkets.bar_scan.config;

import java.util.Properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
public class MailConfig {

    private static final int IMPLICIT_SSL_SMTP_PORT = 465;
    private static final String TIMEOUT_MILLISECONDS = "5000";

    @Bean
    public JavaMailSender javaMailSender(
            @Value("${spring.mail.host:}") String host,
            @Value("${spring.mail.port:587}") int port,
            @Value("${spring.mail.username:}") String username,
            @Value("${spring.mail.password:}") String password
    ) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(host);
        mailSender.setPort(port);
        mailSender.setUsername(username);
        mailSender.setPassword(password);

        Properties properties = mailSender.getJavaMailProperties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.connectiontimeout", TIMEOUT_MILLISECONDS);
        properties.put("mail.smtp.timeout", TIMEOUT_MILLISECONDS);
        properties.put("mail.smtp.writetimeout", TIMEOUT_MILLISECONDS);

        if (port == IMPLICIT_SSL_SMTP_PORT) {
            properties.put("mail.smtp.ssl.enable", "true");
            properties.put("mail.smtp.starttls.enable", "false");
            properties.put("mail.smtp.starttls.required", "false");
        } else {
            properties.put("mail.smtp.ssl.enable", "false");
            properties.put("mail.smtp.starttls.enable", "true");
            properties.put("mail.smtp.starttls.required", "true");
        }

        return mailSender;
    }
}
