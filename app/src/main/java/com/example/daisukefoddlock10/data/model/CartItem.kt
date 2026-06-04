package com.example.daisukefoddlock10.data.model

import java.util.UUID

data class CartItem(
    val id: String = UUID.randomUUID().toString(),
    val food: FoodItem,
    val quantity: Int,
    val size: PortionSize,
    val toppings: Set<Topping>,
    val spicyLevel: Float,
    val itemTotalPrice: Int // (base + size_extra + toppings_total) * quantity
)
