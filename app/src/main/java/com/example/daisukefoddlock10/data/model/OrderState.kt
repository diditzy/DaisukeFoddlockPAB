package com.example.daisukefoddlock10.data.model

enum class PortionSize { REGULAR, LARGE }
enum class Topping { EGG, SAUSAGE, CHEESE, MUSHROOM }

data class OrderState(
    val selectedFood: FoodItem = foodMenuList[0],
    val selectedSize: PortionSize = PortionSize.REGULAR,
    val spicyLevel: Float = 0f,
    val toppings: Set<Topping> = emptySet(),
    val isTakeaway: Boolean = false,
    val notes: String = "",
    val appliedVoucher: PromoVoucher? = null
) {
    val subtotal: Int
        get() = selectedFood.basePrice +
                (if (selectedSize == PortionSize.LARGE) 5000 else 0) +
                (toppings.size * 3000) +
                (if (isTakeaway) 2000 else 0)

    val discount: Int
        get() = appliedVoucher?.discountAmount ?: 0

    val totalPrice: Int
        get() = maxOf(0, subtotal - discount)
}
