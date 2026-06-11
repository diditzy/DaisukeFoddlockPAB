package com.example.daisukefoddlock.dto;

import lombok.Data;
import java.util.List;

@Data
public class CreateOrderRequest {
    private Integer totalPrice;
    private Boolean isDelivery;
    private Boolean isTakeaway;
    private String deliveryAddress;
    private String notes;
    private List<OrderItemRequest> items;
}
