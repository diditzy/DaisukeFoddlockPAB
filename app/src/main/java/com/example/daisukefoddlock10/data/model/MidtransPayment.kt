package com.example.daisukefoddlock10.data.model

data class MidtransOrderItemRequest(
    val foodId: Int,
    val foodName: String,
    val quantity: Int,
    val size: String,
    val toppings: List<String>,
    val spicyLevel: Float,
    val itemTotalPrice: Int
)

data class MidtransOrderRequest(
    val totalAmount: Double,
    val items: List<MidtransOrderItemRequest>
)

data class MidtransApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T?
)

data class MidtransOrderResponse(
    val id: Long,
    val totalAmount: Double,
    val status: String,
    val createdAt: String,
    val snapToken: String
)
