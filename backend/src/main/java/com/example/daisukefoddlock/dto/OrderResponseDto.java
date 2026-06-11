package com.example.daisukefoddlock.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class OrderResponseDto {
    private String id;
    private String createdAt;
    private Integer totalPrice;
    private String status;
    private Boolean isDelivery;
    private Boolean isTakeaway;
    private String deliveryAddress;
    private String notes;
    private String snapToken;
    private String snapRedirectUrl;
    private List<OrderItemRequest> items;
}
