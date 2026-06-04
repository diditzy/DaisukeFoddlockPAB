package com.example.daisukefoddlock10.data.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class CartItem(
    val id: String = UUID.randomUUID().toString(),
    val food: FoodItem,
    val quantity: Int,
    val size: PortionSize,
    val toppings: Set<Topping>,
    val spicyLevel: Float,
    val itemTotalPrice: Int // (base + size_extra + toppings_total) * quantity
)
