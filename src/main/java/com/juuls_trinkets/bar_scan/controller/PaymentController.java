package com.juuls_trinkets.bar_scan.controller;

import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.juuls_trinkets.bar_scan.dto.PaymentCompleteResponseDTO;
import com.juuls_trinkets.bar_scan.service.signuppayment.SignupPaymentApplicationService;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final SignupPaymentApplicationService signupPaymentService;

    public PaymentController(SignupPaymentApplicationService signupPaymentService) {
        this.signupPaymentService = signupPaymentService;
    }

    @PostMapping("/dev/complete/{paymentSessionId}")
    public PaymentCompleteResponseDTO completeDevPayment(@PathVariable UUID paymentSessionId) {
        return signupPaymentService.completeDevPayment(paymentSessionId);
    }

    @GetMapping(value = "/dev/checkout/{paymentSessionId}", produces = MediaType.TEXT_HTML_VALUE)
    public String devCheckout(@PathVariable UUID paymentSessionId) {
        return """
                <!doctype html>
                <html lang="en">
                <head>
                    <meta charset="utf-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1">
                    <title>Dev Checkout</title>
                    <style>
                        body { font-family: Arial, sans-serif; margin: 2rem; max-width: 40rem; }
                        button { padding: .75rem 1rem; cursor: pointer; }
                        code { background: #f4f4f4; padding: .2rem .4rem; }
                    </style>
                </head>
                <body>
                    <h1>Dev Checkout</h1>
                    <p>Payment session: <code>%s</code></p>
                    <button id="pay">Mark payment as paid</button>
                    <pre id="result"></pre>
                    <script>
                        document.getElementById('pay').addEventListener('click', async () => {
                            const response = await fetch('/payments/dev/complete/%s', { method: 'POST' });
                            document.getElementById('result').textContent = await response.text();
                        });
                    </script>
                </body>
                </html>
                """.formatted(paymentSessionId, paymentSessionId);
    }
}
