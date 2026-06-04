package com.example.daisukefoddlock10.ui.screens

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.daisukefoddlock10.data.local.dao.OrderDao
import com.example.daisukefoddlock10.data.local.entity.OrderEntity
import com.example.daisukefoddlock10.data.model.*
import com.example.daisukefoddlock10.data.repository.LogisticsRepository
import com.example.daisukefoddlock10.formatTimestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.util.UUID

@HiltViewModel
class SharedOrderViewModel @Inject constructor(
    private val orderDao: OrderDao,
    private val logisticsRepository: LogisticsRepository
) : ViewModel() {
    private val _orderState = MutableStateFlow(OrderState())
    val orderState: StateFlow<OrderState> = _orderState.asStateFlow()

    private val _activeOrderLogistics = MutableStateFlow<OrderLogistics?>(null)
    val activeOrderLogistics: StateFlow<OrderLogistics?> = _activeOrderLogistics.asStateFlow()

    private val _remainingMinutes = MutableStateFlow<Int>(20)
    val remainingMinutes: StateFlow<Int> = _remainingMinutes.asStateFlow()

    private var countdownJob: kotlinx.coroutines.Job? = null

    fun startCountdown() {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            while (_remainingMinutes.value > 0) {
                kotlinx.coroutines.delay(60000) // 1 minute
                _remainingMinutes.update { it - 1 }
            }
        }
    }

    private fun stopCountdown() {
        countdownJob?.cancel()
        _remainingMinutes.value = 20
    }

    // Ambil data dari Room Database (Real-time Flow)
    val orderHistory: StateFlow<List<OrderHistory>> = orderDao.getAllOrders()
        .map { entities ->
            entities.map { entity ->
                OrderHistory(
                    orderId = entity.transactionId,
                    food = foodMenuList.find { it.name == entity.foodName } ?: foodMenuList[0],
                    size = try { PortionSize.valueOf(entity.size) } catch (e: Exception) { PortionSize.REGULAR },
                    toppings = entity.toppings.split(",")
                        .filter { it.isNotEmpty() }
                        .mapNotNull { name -> try { Topping.valueOf(name) } catch (e: Exception) { null } }
                        .toSet(),
                    notes = entity.notes,
                    quantity = entity.quantity,
                    totalPrice = entity.price.toInt(),
                    paymentMethod = entity.paymentMethod,
                    status = try { OrderStatus.valueOf(entity.status) } catch (e: Exception) { OrderStatus.COMPLETED },
                    date = formatTimestamp(entity.timestamp)
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun updateQuantity(delta: Int) {
        _orderState.update { it.copy(quantity = maxOf(1, it.quantity + delta)) }
    }

    fun addToCart() {
        _orderState.update { current ->
            // Check for existing identical item in cart
            val existingItemIndex = current.cartItems.indexOfFirst {
                it.food.id == current.selectedFood.id &&
                it.size == current.selectedSize &&
                it.toppings == current.toppings &&
                it.spicyLevel == current.spicyLevel
            }

            val updatedCart = if (existingItemIndex != -1) {
                // Update quantity and price of existing item
                val existingItem = current.cartItems[existingItemIndex]
                val newQuantity = existingItem.quantity + current.quantity
                val unitPrice = current.selectedFood.basePrice +
                        (if (current.selectedSize == PortionSize.LARGE) 5000 else 0) +
                        (current.toppings.size * 3000)
                
                current.cartItems.toMutableList().apply {
                    this[existingItemIndex] = existingItem.copy(
                        quantity = newQuantity,
                        itemTotalPrice = unitPrice * newQuantity
                    )
                }
            } else {
                // Add new unique item
                val newItem = CartItem(
                    food = current.selectedFood,
                    quantity = current.quantity,
                    size = current.selectedSize,
                    toppings = current.toppings,
                    spicyLevel = current.spicyLevel,
                    itemTotalPrice = current.currentSelectionPrice
                )
                current.cartItems + newItem
            }
            
            current.copy(
                cartItems = updatedCart,
                // Reset selection for next item
                selectedFood = foodMenuList[0],
                selectedSize = PortionSize.REGULAR,
                spicyLevel = 0f,
                toppings = emptySet(),
                quantity = 1
            )
        }
    }

    fun removeFromCart(itemId: String) {
        _orderState.update { it.copy(cartItems = it.cartItems.filter { item -> item.id != itemId }) }
    }

    fun clearCart() {
        _orderState.update { it.copy(cartItems = emptyList()) }
    }

    fun selectFood(food: FoodItem) {
        _orderState.update { 
            it.copy(
                selectedFood = food,
                spicyLevel = 0f, // Reset spicy level
                toppings = emptySet(), // Reset toppings
                quantity = 1 // Reset quantity
            ) 
        }
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

    fun updateDelivery(isDelivery: Boolean) {
        _orderState.update { it.copy(isDelivery = isDelivery, isTakeaway = if (isDelivery) false else it.isTakeaway) }
    }

    fun updateTakeaway(isTakeaway: Boolean) {
        _orderState.update { it.copy(isTakeaway = isTakeaway, isDelivery = if (isTakeaway) false else it.isDelivery) }
    }

    fun updateNotes(notes: String) {
        _orderState.update { it.copy(notes = notes) }
    }

    fun updateDeliveryAddress(address: String) {
        _orderState.update { it.copy(deliveryAddress = address) }
    }

    fun applyVoucher(voucher: PromoVoucher?) {
        _orderState.update { it.copy(appliedVoucher = voucher) }
    }

    fun confirmOrder(paymentMethod: String): String {
        val transactionId = UUID.randomUUID().toString().take(8).uppercase()
        val current = _orderState.value
        
        if (current.cartItems.isEmpty()) return ""

        val totalSubtotal = current.cartSubtotal
        val discount = current.discount

        viewModelScope.launch {
            try {
                // 1. Simpan ke Room Database
                current.cartItems.forEach { item ->
                    val itemProportion = if (totalSubtotal > 0) item.itemTotalPrice.toDouble() / totalSubtotal else 0.0
                    val itemDiscount = discount * itemProportion
                    val finalItemPrice = item.itemTotalPrice - itemDiscount

                    orderDao.insertOrder(
                        OrderEntity(
                            transactionId = transactionId,
                            foodName = item.food.name,
                            quantity = item.quantity,
                            price = finalItemPrice,
                            size = item.size.name,
                            toppings = item.toppings.joinToString(",") { it.name },
                            notes = current.notes,
                            paymentMethod = paymentMethod,
                            status = OrderStatus.CONFIRMED.name
                        )
                    )
                }

                // 2. Bersihkan keranjang
                clearCart()
                
                // 3. Inisialisasi Logistik
                val mockLogistics = logisticsRepository.getMockLogistics(transactionId)
                _activeOrderLogistics.value = mockLogistics
                startCountdown()
                
                launch {
                    logisticsRepository.observeOrderStatus(transactionId)
                        .catch { e -> Log.e("ORDER", "Logistics observation failed: ${e.message}") }
                        .collect { newStatus ->
                            _activeOrderLogistics.update { it?.copy(status = newStatus) }
                            if (newStatus == OrderLogisticsStatus.DELIVERED) {
                                stopCountdown()
                            }
                        }
                }
            } catch (e: Exception) {
                Log.e("ORDER", "Error during order confirmation: ${e.message}")
            }
        }

        Log.d("ORDER", "Confirmed #$transactionId via $paymentMethod | Total: ${current.totalPrice}")
        return transactionId
    }

    fun resetOrder() {
        _orderState.value = OrderState()
    }

    fun clearHistory() {
        viewModelScope.launch {
            orderDao.deleteAllOrders()
        }
    }
}
