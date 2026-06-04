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
    val orderId: String,
    val food: FoodItem,
    val size: PortionSize,
    val toppings: Set<Topping>,
    val notes: String,
    val quantity: Int = 1,
    val totalPrice: Int,
    val paymentMethod: String,
    val status: OrderStatus = OrderStatus.CONFIRMED,
    val date: String,
    val appliedVoucher: String? = null
)

val dummyOrderHistoryList = listOf(
    OrderHistory(
        orderId = "A1B2C3D4",
        food = foodMenuList[0],
        size = PortionSize.LARGE,
        toppings = setOf(Topping.EGG, Topping.CHEESE),
        notes = "Jangan pakai bawang",
        totalPrice = 57000,
        paymentMethod = "GoPay",
        status = OrderStatus.COMPLETED,
        date = "10 Oct 2023, 14:30"
    ),
    OrderHistory(
        orderId = "E5F6G7H8",
        food = foodMenuList[4],
        size = PortionSize.REGULAR,
        toppings = setOf(Topping.SAUSAGE),
        notes = "",
        totalPrice = 43000,
        paymentMethod = "QRIS",
        status = OrderStatus.PREPARING,
        date = "10 Oct 2023, 15:45"
    )
)
