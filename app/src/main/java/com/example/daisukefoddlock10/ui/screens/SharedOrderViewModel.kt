package com.example.daisukefoddlock10.ui.screens

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.daisukefoddlock10.data.local.dao.OrderDao
import com.example.daisukefoddlock10.data.local.entity.OrderEntity
import com.example.daisukefoddlock10.data.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.util.UUID

@HiltViewModel
class SharedOrderViewModel @Inject constructor(
    private val orderDao: OrderDao
) : ViewModel() {
    private val _orderState = MutableStateFlow(OrderState())
    val orderState: StateFlow<OrderState> = _orderState.asStateFlow()

    // Ambil data dari Room Database (Real-time Flow)
    val orderHistory: StateFlow<List<OrderHistory>> = orderDao.getAllOrders()
        .map { entities ->
            entities.map { entity ->
                OrderHistory(
                    orderId = entity.id.toString(),
                    food = foodMenuList.find { it.name == entity.foodName } ?: foodMenuList[0],
                    size = PortionSize.REGULAR,
                    toppings = emptySet(),
                    isTakeaway = false,
                    notes = "",
                    totalPrice = entity.price.toInt(),
                    paymentMethod = "Tunai",
                    status = OrderStatus.COMPLETED,
                    timestamp = entity.timestamp
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

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
        
        // Simpan ke Room Database
        viewModelScope.launch {
            orderDao.insertOrder(
                OrderEntity(
                    foodName = current.selectedFood.name,
                    price = current.totalPrice.toDouble()
                )
            )
        }

        Log.d("ORDER", "Confirmed #$orderId via $paymentMethod | Total: ${current.totalPrice}")
        return orderId
    }

    fun resetOrder() {
        _orderState.value = OrderState()
    }

    fun clearHistory() {
    }
}
