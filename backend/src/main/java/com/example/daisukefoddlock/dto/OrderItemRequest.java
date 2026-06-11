package com.example.daisukefoddlock.dto;

import lombok.Data;
import java.util.List;

@Data
public class OrderItemRequest {
    private Integer foodId;
    private String foodName;
    private Integer quantity;
    private String size; // REGULAR, LARGE
    private List<String> toppings;
    private Float spicyLevel;
    private Integer itemTotalPrice;
}
