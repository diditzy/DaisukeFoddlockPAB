package com.example.daisukefoddlock.controller;

import com.example.daisukefoddlock.dto.ApiResponse;
import com.example.daisukefoddlock.dto.OrderRequest;
import com.example.daisukefoddlock.dto.OrderResponse;
import com.example.daisukefoddlock.entity.User;
import com.example.daisukefoddlock.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @Valid @RequestBody OrderRequest request,
            @AuthenticationPrincipal User user) {
        
        OrderResponse response = orderService.createOrder(request, user);
        return ResponseEntity.ok(ApiResponse.success("Order created successfully", response));
    }
}
