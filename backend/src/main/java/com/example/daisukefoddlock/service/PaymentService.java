package com.example.daisukefoddlock.service;

import com.example.daisukefoddlock.config.MidtransProperties;
import com.example.daisukefoddlock.dto.MidtransWebhookRequest;
import com.example.daisukefoddlock.entity.Order;
import com.example.daisukefoddlock.entity.Payment;
import com.example.daisukefoddlock.repository.OrderRepository;
import com.example.daisukefoddlock.repository.PaymentRepository;
import com.midtrans.service.MidtransCoreApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final MidtransCoreApi midtransCoreApi;
    private final MidtransProperties properties;

    @Transactional
    public void handleWebhook(MidtransWebhookRequest request) {
        log.info("Received webhook for Midtrans Order ID: {}", request.getOrderId());

        // 1. Webhook Security: Verify Signature Key (First Gatekeeper)
        if (!verifySignature(request)) {
            log.error("CRITICAL: Invalid signature for order: {}. This might be a spoofing attempt.", request.getOrderId());
            throw new RuntimeException("Invalid signature key");
        }

        // 2. Find Order using externalOrderId
        Order order = orderRepository.findByExternalOrderId(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found: " + request.getOrderId()));

        // 3. Double Verification: Verify status directly with Midtrans API
        try {
            Map<String, Object> statusResponse = midtransCoreApi.checkTransaction(request.getOrderId());
            String actualStatus = (String) statusResponse.get("transaction_status");
            String fraudStatus = (String) statusResponse.get("fraud_status");
            
            log.info("Verified transaction status: {} for order: {}", actualStatus, request.getOrderId());

            // 4. Update Payment audit record
            Payment payment = paymentRepository.findByOrder(order)
                    .orElse(Payment.builder().order(order).build());

            payment.setMidtransTransactionId((String) statusResponse.get("transaction_id"));
            payment.setPaymentMethod((String) statusResponse.get("payment_type"));
            payment.setMidtransStatus(actualStatus);
            
            // 5. Map to Business Status
            mapAndApplyStatus(order, payment, actualStatus, fraudStatus);

            paymentRepository.save(payment);
            orderRepository.save(order);
            
        } catch (Exception e) {
            log.error("Failed to verify transaction with Midtrans API: {}", e.getMessage());
            throw new RuntimeException("Payment verification failed", e);
        }
    }

    private void mapAndApplyStatus(Order order, Payment payment, String midtransStatus, String fraudStatus) {
        switch (midtransStatus) {
            case "capture" -> {
                if ("challenge".equals(fraudStatus)) {
                    applyStatus(order, payment, Payment.PaymentStatus.PENDING, Order.OrderStatus.PENDING);
                } else {
                    applyStatus(order, payment, Payment.PaymentStatus.PAID, Order.OrderStatus.PAID);
                    payment.setPaidAt(LocalDateTime.now());
                }
            }
            case "settlement" -> {
                applyStatus(order, payment, Payment.PaymentStatus.PAID, Order.OrderStatus.PAID);
                payment.setPaidAt(LocalDateTime.now());
            }
            case "pending" -> applyStatus(order, payment, Payment.PaymentStatus.PENDING, Order.OrderStatus.PENDING);
            case "deny", "cancel", "failure" -> {
                applyStatus(order, payment, Payment.PaymentStatus.FAILED, Order.OrderStatus.CANCELLED);
            }
            case "expire" -> applyStatus(order, payment, Payment.PaymentStatus.EXPIRED, Order.OrderStatus.EXPIRED);
            default -> log.warn("Unhandled Midtrans status: {}", midtransStatus);
        }
    }

    private void applyStatus(Order order, Payment payment, Payment.PaymentStatus pStatus, Order.OrderStatus oStatus) {
        payment.setStatus(pStatus);
        order.setStatus(oStatus);
    }

    private boolean verifySignature(MidtransWebhookRequest request) {
        // SHA512(order_id + status_code + gross_amount + ServerKey)
        String raw = request.getOrderId() + request.getStatusCode() + request.getGrossAmount() + properties.getServerKey();
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            byte[] digest = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString().equalsIgnoreCase(request.getSignatureKey());
        } catch (NoSuchAlgorithmException e) {
            log.error("SHA-512 algorithm not found", e);
            return false;
        }
    }
}
