package com.example.daisukefoddlock.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "order_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonIgnore
    @ToString.Exclude
    private Order order;

    @Column(name = "food_id", nullable = false)
    private Integer foodId;

    @Column(name = "food_name", nullable = false)
    private String foodName;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private String size; // REGULAR, LARGE

    @Column(name = "spicy_level", nullable = false)
    private Float spicyLevel;

    @Column(name = "item_total_price", nullable = false)
    private Integer itemTotalPrice;

    private String toppings; // comma-separated strings
}
