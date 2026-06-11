package com.example.daisukefoddlock.controller;

import com.example.daisukefoddlock.dto.MidtransWebhookRequest;
import com.example.daisukefoddlock.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/webhook")
    public ResponseEntity<Void> handleMidtransWebhook(@RequestBody MidtransWebhookRequest request) {
        paymentService.handleWebhook(request);
        return ResponseEntity.ok().build();
    }
}
