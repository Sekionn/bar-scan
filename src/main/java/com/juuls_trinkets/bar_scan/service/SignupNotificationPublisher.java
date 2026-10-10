package com.juuls_trinkets.bar_scan.service;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.juuls_trinkets.bar_scan.config.RabbitMqTopology;
import com.juuls_trinkets.bar_scan.dto.ContactSignupRequestDTO;
import com.juuls_trinkets.bar_scan.dto.SignupNotificationEventDTO;

@Service
public class SignupNotificationPublisher {

    private static final Logger logger = LoggerFactory.getLogger(SignupNotificationPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    public SignupNotificationPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishAfterCommit(UUID signupId, ContactSignupRequestDTO request, String adminEmail) {
        SignupNotificationEventDTO event = new SignupNotificationEventDTO(
                signupId,
                request.getCompanyName().trim(),
                request.getLocationName().trim(),
                request.getAllowedUserCount(),
                adminEmail,
                normalizeMessage(request.getMessage())
        );

        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            publish(event);
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                publish(event);
            }
        });
    }

    private void publish(SignupNotificationEventDTO event) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMqTopology.SIGNUP_NOTIFICATION_EXCHANGE,
                    RabbitMqTopology.SIGNUP_NOTIFICATION_ROUTING_KEY,
                    event
            );
        } catch (RuntimeException ex) {
            logger.warn("Signup notification event could not be published for signupId={}", event.signupId(), ex);
        }
    }

    private static String normalizeMessage(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return value.trim();
    }
}
