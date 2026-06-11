package com.example.daisukefoddlock.service;

import com.example.daisukefoddlock.dto.OrderRequest;
import com.example.daisukefoddlock.dto.OrderResponse;
import com.example.daisukefoddlock.entity.Order;
import com.example.daisukefoddlock.entity.OrderItem;
import com.example.daisukefoddlock.entity.Payment;
import com.example.daisukefoddlock.entity.User;
import com.example.daisukefoddlock.repository.OrderRepository;
import com.example.daisukefoddlock.repository.PaymentRepository;
import com.midtrans.service.MidtransSnapApi;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final MidtransSnapApi midtransSnapApi;

    @Transactional
    public OrderResponse createOrder(OrderRequest request, User customer) {
        // 1. Validate that items exist and sum of item prices matches totalAmount
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Order items list cannot be empty");
        }
        
        BigDecimal calculatedTotal = BigDecimal.ZERO;
        for (com.example.daisukefoddlock.dto.OrderItemRequest itemReq : request.getItems()) {
            if (itemReq.getItemTotalPrice() == null) {
                throw new IllegalArgumentException("Item total price is required");
            }
            calculatedTotal = calculatedTotal.add(BigDecimal.valueOf(itemReq.getItemTotalPrice()));
        }
        
        if (request.getTotalAmount().compareTo(calculatedTotal) != 0) {
            throw new IllegalArgumentException("Total amount does not match the sum of item prices");
        }

        // 2. Generate a unique external order ID for Midtrans
        String externalOrderId = "ORDER-" + UUID.randomUUID().toString().toUpperCase().substring(0, 8);

        // 3. Create order and link items for cascading save
        Order order = Order.builder()
                .customer(customer)
                .totalAmount(request.getTotalAmount())
                .status(Order.OrderStatus.PENDING)
                .externalOrderId(externalOrderId)
                .build();
        
        for (com.example.daisukefoddlock.dto.OrderItemRequest itemReq : request.getItems()) {
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .foodId(itemReq.getFoodId())
                    .foodName(itemReq.getFoodName())
                    .quantity(itemReq.getQuantity())
                    .size(itemReq.getSize())
                    .spicyLevel(itemReq.getSpicyLevel())
                    .itemTotalPrice(itemReq.getItemTotalPrice())
                    .toppings(itemReq.getToppings() != null ? String.join(",", itemReq.getToppings()) : "")
                    .build();
            order.getItems().add(orderItem);
        }
        
        order = orderRepository.save(order);

        // 3. Prepare Midtrans Snap Request
        Map<String, Object> params = new HashMap<>();

        Map<String, String> transactionDetails = new HashMap<>();
        transactionDetails.put("order_id", externalOrderId);
        transactionDetails.put("gross_amount", request.getTotalAmount().toPlainString());

        params.put("transaction_details", transactionDetails);

        Map<String, String> customerDetails = new HashMap<>();
        customerDetails.put("first_name", customer.getName());
        customerDetails.put("email", customer.getEmail());
        params.put("customer_details", customerDetails);

        try {
            // 4. Generate Snap Token
            String snapToken = midtransSnapApi.createTransactionToken(params);
            
            // 5. Store Snap Token in Payment entity for audit/troubleshooting
            Payment payment = Payment.builder()
                    .order(order)
                    .snapToken(snapToken)
                    .status(Payment.PaymentStatus.PENDING)
                    .build();
            paymentRepository.save(payment);

            return OrderResponse.fromEntity(order, snapToken);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate Midtrans Snap Token: " + e.getMessage());
        }
    }
}
