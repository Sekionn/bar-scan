package com.juuls_trinkets.bar_scan.events.signup;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.juuls_trinkets.bar_scan.config.RabbitMqTopology;
import com.juuls_trinkets.bar_scan.dto.SignupNotificationEventDTO;
import com.juuls_trinkets.bar_scan.service.SignupNotificationService;

import jakarta.validation.Valid;

@Component
public class SignupNotificationMessageListener {

    private final SignupNotificationService signupNotificationService;

    public SignupNotificationMessageListener(SignupNotificationService signupNotificationService) {
        this.signupNotificationService = signupNotificationService;
    }

    @RabbitListener(queues = RabbitMqTopology.SIGNUP_NOTIFICATION_QUEUE)
    public void handle(@Valid SignupNotificationEventDTO event) {
        signupNotificationService.sendSignupNotification(event);
    }
}
