package com.juuls_trinkets.bar_scan.controller;

import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.juuls_trinkets.bar_scan.dto.ContactSignupRequestDTO;
import com.juuls_trinkets.bar_scan.dto.ContactSignupResponseDTO;
import com.juuls_trinkets.bar_scan.dto.SignupCheckoutResponseDTO;
import com.juuls_trinkets.bar_scan.dto.StartSignupRequestDTO;
import com.juuls_trinkets.bar_scan.service.contactsignup.ContactSignupApplicationService;
import com.juuls_trinkets.bar_scan.service.signuppayment.SignupPaymentApplicationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/signup")
public class SignupController {

    private final SignupPaymentApplicationService signupPaymentService;
    private final ContactSignupApplicationService contactSignupService;

    public SignupController(
            SignupPaymentApplicationService signupPaymentService,
            ContactSignupApplicationService contactSignupService
    ) {
        this.signupPaymentService = signupPaymentService;
        this.contactSignupService = contactSignupService;
    }

    @PostMapping("/contact")
    public ContactSignupResponseDTO contact(@Valid @RequestBody ContactSignupRequestDTO request) {
        return contactSignupService.requestContactSignup(request);
    }

    @PostMapping("/start")
    public SignupCheckoutResponseDTO start(@Valid @RequestBody StartSignupRequestDTO request) {
        return signupPaymentService.startSignup(request);
    }

    @GetMapping("/{signupId}")
    public SignupCheckoutResponseDTO get(@PathVariable UUID signupId) {
        return signupPaymentService.findSignup(signupId);
    }
}
