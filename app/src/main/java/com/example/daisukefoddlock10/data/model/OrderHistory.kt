package com.example.daisukefoddlock10.data.model

import java.util.UUID

enum class OrderStatus {
    PENDING,        // Menunggu konfirmasi
    CONFIRMED,      // Dikonfirmasi restoran
    PREPARING,      // Sedang dimasak
    READY,          // Siap diambil/diantar
    COMPLETED,      // Selesai
    CANCELLED       // Dibatalkan
}

data class OrderHistory(
    val orderId: String = UUID.randomUUID().toString().take(8).uppercase(),
    val food: FoodItem,
    val size: PortionSize,
    val toppings: Set<Topping>,
    val isTakeaway: Boolean,
    val notes: String,
    val totalPrice: Int,
    val paymentMethod: String,
    val status: OrderStatus = OrderStatus.CONFIRMED,
    val timestamp: Long = System.currentTimeMillis(),
    val appliedVoucher: String? = null
)

val dummyOrderHistoryList = listOf(
    OrderHistory(
        orderId = "A1B2C3D4",
        food = foodMenuList[0],
        size = PortionSize.LARGE,
        toppings = setOf(Topping.EGG, Topping.CHEESE),
        isTakeaway = false,
        notes = "Jangan pakai bawang",
        totalPrice = 57000,
        paymentMethod = "GoPay",
        status = OrderStatus.COMPLETED,
        timestamp = System.currentTimeMillis() - 3_600_000
    ),
    OrderHistory(
        orderId = "E5F6G7H8",
        food = foodMenuList[4],
        size = PortionSize.REGULAR,
        toppings = setOf(Topping.SAUSAGE),
        isTakeaway = true,
        notes = "",
        totalPrice = 43000,
        paymentMethod = "QRIS",
        status = OrderStatus.PREPARING,
        timestamp = System.currentTimeMillis() - 7_200_000
    )
)
