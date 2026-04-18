package com.example.daisukefoddlock10.ui.screens

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.daisukefoddlock10.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

class SharedOrderViewModel : ViewModel() {
    private val _orderState = MutableStateFlow(OrderState())
    val orderState: StateFlow<OrderState> = _orderState.asStateFlow()

    private val _orderHistory = MutableStateFlow<List<OrderHistory>>(dummyOrderHistoryList)
    val orderHistory: StateFlow<List<OrderHistory>> = _orderHistory.asStateFlow()

    fun selectFood(food: FoodItem) {
        _orderState.update { it.copy(selectedFood = food) }
    }

    fun updateSize(size: PortionSize) {
        _orderState.update { it.copy(selectedSize = size) }
    }

    fun updateSpicyLevel(level: Float) {
        _orderState.update { it.copy(spicyLevel = level) }
    }

    fun toggleTopping(topping: Topping) {
        _orderState.update { current ->
            val newToppings = if (current.toppings.contains(topping)) {
                current.toppings - topping
            } else {
                current.toppings + topping
            }
            current.copy(toppings = newToppings)
        }
    }

    fun updateTakeaway(isTakeaway: Boolean) {
        _orderState.update { it.copy(isTakeaway = isTakeaway) }
    }

    fun updateNotes(notes: String) {
        _orderState.update { it.copy(notes = notes) }
    }

    fun applyVoucher(voucher: PromoVoucher?) {
        _orderState.update { it.copy(appliedVoucher = voucher) }
    }

    fun confirmOrder(paymentMethod: String): String {
        val orderId = UUID.randomUUID().toString().take(8).uppercase()
        val current = _orderState.value
        val newOrder = OrderHistory(
            orderId = orderId,
            food = current.selectedFood,
            size = current.selectedSize,
            toppings = current.toppings,
            isTakeaway = current.isTakeaway,
            notes = current.notes,
            totalPrice = current.totalPrice,
            paymentMethod = paymentMethod,
            appliedVoucher = current.appliedVoucher?.title
        )
        _orderHistory.update { listOf(newOrder) + it }
        Log.d("ORDER", "Confirmed #$orderId via $paymentMethod | Total: ${current.totalPrice}")
        return orderId
    }

    fun resetOrder() {
        _orderState.value = OrderState()
    }
}
