package com.example.daisukefoddlock10.data.model

enum class PortionSize { REGULAR, LARGE }

enum class Topping(val category: ToppingCategory) {
    // Savory Toppings
    EGG(ToppingCategory.SAVORY),
    SAUSAGE(ToppingCategory.SAVORY),
    CHEESE(ToppingCategory.SAVORY),
    MUSHROOM(ToppingCategory.SAVORY),
    
    // Sweet Toppings
    MATCHA_POWDER(ToppingCategory.SWEET),
    ICE_CREAM(ToppingCategory.SWEET),
    CHOCO_CHIPS(ToppingCategory.SWEET),
    HONEY(ToppingCategory.SWEET)
}

data class OrderState(
    val selectedFood: FoodItem = foodMenuList[0],
    val selectedSize: PortionSize = PortionSize.REGULAR,
    val spicyLevel: Float = 0f,
    val toppings: Set<Topping> = emptySet(),
    val isDelivery: Boolean = false,
    val isTakeaway: Boolean = false,
    val notes: String = "",
    val deliveryAddress: String = "",
    val appliedVoucher: PromoVoucher? = null,
    val quantity: Int = 1,
    val cartItems: List<CartItem> = emptyList()
) {
    val deliveryFee: Int = 5000
    val takeawayFee: Int = 1000

    val currentSelectionPrice: Int
        get() = (selectedFood.basePrice +
                (if (selectedSize == PortionSize.LARGE) 5000 else 0) +
                (toppings.size * 3000)) * quantity

    val cartSubtotal: Int
        get() = cartItems.sumOf { it.itemTotalPrice }

    val subtotal: Int
        get() = cartSubtotal + (if (isDelivery) deliveryFee else 0) + (if (isTakeaway) takeawayFee else 0)

    val discount: Int
        get() {
            val voucher = appliedVoucher ?: return 0
            return if (cartSubtotal >= voucher.minOrder) voucher.discountAmount else 0
        }

    val totalPrice: Int
        get() = maxOf(0, subtotal - discount)
}
