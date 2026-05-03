package com.example.daisukefoddlock10.domain.model

enum class OrderStatus {
    PENDING,
    PAID,
    PREPARING,
    DELIVERING,
    COMPLETED,
    CANCELLED
}

data class Order(
    val id: String,
    val customerId: String,
    val merchantId: String,
    val courierId: String?,
    val status: OrderStatus,
    val totalPrice: Int,
    val items: List<OrderItem>
)

data class OrderItem(
    val menuId: String,
    val quantity: Int,
    val price: Int
)
