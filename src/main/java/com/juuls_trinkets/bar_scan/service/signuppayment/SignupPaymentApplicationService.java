package com.juuls_trinkets.bar_scan.service.signuppayment;

import java.util.UUID;

import com.juuls_trinkets.bar_scan.dto.PaymentCompleteResponseDTO;
import com.juuls_trinkets.bar_scan.dto.SignupCheckoutResponseDTO;
import com.juuls_trinkets.bar_scan.dto.StartSignupRequestDTO;

public interface SignupPaymentApplicationService {

    SignupCheckoutResponseDTO startSignup(StartSignupRequestDTO request);

    SignupCheckoutResponseDTO findSignup(UUID signupId);

    PaymentCompleteResponseDTO completeDevPayment(UUID paymentSessionId);
}
