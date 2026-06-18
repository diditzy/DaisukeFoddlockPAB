package com.example.daisukefoddlock10.data.model

import kotlinx.serialization.Serializable

@Serializable
data class OrderItemData(
    val food_name: String,
    val food_id: Int,
    val quantity: Int,
    val size: String,
    val toppings: List<String>,
    val spicy_level: Float,
    val item_total_price: Int
)

@Serializable
data class OrderRequest(
    val total_price: Int,
    val status: String,
    val is_delivery: Boolean,
    val is_takeaway: Boolean,
    val delivery_address: String? = null,
    val notes: String? = null,
    val items: List<OrderItemData>
)

@Serializable
data class OrderResponse(
    val id: String,
    val created_at: String? = null,
    val total_price: Int,
    val status: String,
    val is_delivery: Boolean,
    val is_takeaway: Boolean,
    val delivery_address: String? = null,
    val notes: String? = null,
    val items: List<OrderItemData> = emptyList()
)
