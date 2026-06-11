package com.example.daisukefoddlock10.ui.screens

import android.content.Context
import android.util.Log
import com.example.daisukefoddlock10.BuildConfig
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.daisukefoddlock10.data.local.dao.OrderDao
import com.example.daisukefoddlock10.data.local.entity.OrderEntity
import com.example.daisukefoddlock10.data.model.*
import com.example.daisukefoddlock10.data.remote.ApiService
import com.example.daisukefoddlock10.data.repository.LogisticsRepository
import com.example.daisukefoddlock10.data.repository.OrderRepository
import com.example.daisukefoddlock10.formatTimestamp
import com.midtrans.sdk.uikit.external.SdkUIFlowBuilder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.util.UUID

@HiltViewModel
class SharedOrderViewModel @Inject constructor(
    private val orderDao: OrderDao,
    private val logisticsRepository: LogisticsRepository,
    private val orderRepository: OrderRepository,
    private val apiService: ApiService
) : ViewModel() {
    private val _orderState = MutableStateFlow(OrderState())
    val orderState: StateFlow<OrderState> = _orderState.asStateFlow()

    private val _activeOrderLogistics = MutableStateFlow<OrderLogistics?>(null)
    val activeOrderLogistics: StateFlow<OrderLogistics?> = _activeOrderLogistics.asStateFlow()

    private val _remainingMinutes = MutableStateFlow<Int>(20)
    val remainingMinutes: StateFlow<Int> = _remainingMinutes.asStateFlow()

    private var countdownJob: kotlinx.coroutines.Job? = null
    private var statusPollingJob: kotlinx.coroutines.Job? = null

    // State untuk Payment Loading
    private val _isPaymentProcessing = MutableStateFlow(false)
    val isPaymentProcessing = _isPaymentProcessing.asStateFlow()

    private val _paymentSuccessEvent = MutableSharedFlow<Pair<String, String>>()
    val paymentSuccessEvent = _paymentSuccessEvent.asSharedFlow()

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
        statusPollingJob?.cancel()
        _remainingMinutes.value = 20
        _activeOrderLogistics.value = null
    }

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
            val existingItemIndex = current.cartItems.indexOfFirst {
                it.food.id == current.selectedFood.id &&
                it.size == current.selectedSize &&
                it.toppings == current.toppings &&
                it.spicyLevel == current.spicyLevel
            }

            val updatedCart = if (existingItemIndex != -1) {
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
                spicyLevel = 0f,
                toppings = emptySet(),
                quantity = 1
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

    /**
     * Integrasi Midtrans:
     * 1. Minta Snap Token dari Backend Spring Boot
     * 2. Buka UI Midtrans Snap di Android
     */
    fun startMidtransPayment(context: Context) {
        val current = _orderState.value
        if (current.cartItems.isEmpty()) return

        _isPaymentProcessing.value = true

        viewModelScope.launch {
            try {
                // 1. Panggil Backend Spring Boot untuk mendapatkan Snap Token
                val midtransItems = current.cartItems.map { item ->
                    MidtransOrderItemRequest(
                        foodId = item.food.id,
                        foodName = item.food.name,
                        quantity = item.quantity,
                        size = item.size.name,
                        toppings = item.toppings.map { it.name },
                        spicyLevel = item.spicyLevel,
                        itemTotalPrice = item.itemTotalPrice
                    )
                }
                val response = apiService.createMidtransOrder(
                    MidtransOrderRequest(
                        totalAmount = current.totalPrice.toDouble(),
                        items = midtransItems
                    )
                )
                
                if (response.success && response.data != null) {
                    val snapToken = response.data.snapToken
                    
                    // 2. Gunakan Midtrans SDK untuk membuka UI Pembayaran
                    SdkUIFlowBuilder.init()
                        .setContext(context)
                        .setMerchantBaseUrl(BuildConfig.BACKEND_URL) // Dynamic Backend URL
                        .setClientKey(BuildConfig.MIDTRANS_CLIENT_KEY)
                        .setTransactionFinishedCallback { result ->
                            // Handle hasil transaksi
                            Log.d("MIDTRANS", "Finished: ${result.status}")
                            if (result.status == "settlement" || result.status == "pending") {
                                // Jika sukses atau pending, anggap pesanan masuk ke sistem
                                confirmOrderAfterPayment(response.data.id.toString())
                            }
                        }
                        .buildSDK()
                    
                    com.midtrans.sdk.uikit.api.model.SnapConfig.builder()
                        .setSnapToken(snapToken)
                        .build()
                        .let { config ->
                            SdkUIFlowBuilder.init().startPaymentUi(context, config)
                        }
                }
            } catch (e: Exception) {
                Log.e("MIDTRANS", "Error during payment process: ${e.message}")
            } finally {
                _isPaymentProcessing.value = false
            }
        }
    }

    private fun confirmOrderAfterPayment(transactionId: String) {
        val current = _orderState.value
        viewModelScope.launch {
            try {
                // Simpan ke Room Database
                current.cartItems.forEach { item ->
                    orderDao.insertOrder(
                        OrderEntity(
                            transactionId = transactionId,
                            foodName = item.food.name,
                            quantity = item.quantity,
                            price = item.itemTotalPrice.toDouble(),
                            size = item.size.name,
                            toppings = item.toppings.joinToString(",") { it.name },
                            notes = current.notes,
                            paymentMethod = "Midtrans",
                            status = OrderStatus.PENDING.name
                        )
                    )
                }
                clearCart()
                startCountdown()
                startStatusPolling(transactionId)
                
                // Beri tahu UI bahwa pembayaran berhasil
                _paymentSuccessEvent.emit(Pair(transactionId, "Midtrans"))
            } catch (e: Exception) {
                Log.e("ORDER", "Failed to confirm order locally", e)
            }
        }
    }

    // Fungsi lama untuk Supabase tetap ada jika ingin digunakan sebagai fallback
    fun confirmOrder(paymentMethod: String, onComplete: (String) -> Unit) {
        val current = _orderState.value
        if (current.cartItems.isEmpty()) return

        viewModelScope.launch {
            var transactionId = ""
            try {
                val orderItems = current.cartItems.map { item ->
                    OrderItemData(
                        food_name = item.food.name,
                        food_id = item.food.id,
                        quantity = item.quantity,
                        size = item.size.name,
                        toppings = item.toppings.map { it.name },
                        spicy_level = item.spicyLevel,
                        item_total_price = item.itemTotalPrice
                    )
                }

                val orderRequest = OrderRequest(
                    total_price = current.totalPrice,
                    status = "PENDING",
                    is_delivery = current.isDelivery,
                    is_takeaway = current.isTakeaway,
                    delivery_address = if (current.isDelivery) current.deliveryAddress else null,
                    notes = current.notes.ifBlank { null },
                    items = orderItems
                )
                
                val result = kotlinx.coroutines.withTimeoutOrNull(5000) {
                    orderRepository.placeOrder(orderRequest)
                }

                if (result != null && result.isSuccess) {
                    transactionId = result.getOrNull()?.id ?: UUID.randomUUID().toString().take(8).uppercase()
                } else {
                    transactionId = UUID.randomUUID().toString().take(8).uppercase()
                }
            } catch (e: Exception) {
                transactionId = UUID.randomUUID().toString().take(8).uppercase()
            }

            try {
                current.cartItems.forEach { item ->
                    orderDao.insertOrder(
                        OrderEntity(
                            transactionId = transactionId,
                            foodName = item.food.name,
                            quantity = item.quantity,
                            price = item.itemTotalPrice.toDouble(),
                            size = item.size.name,
                            toppings = item.toppings.joinToString(",") { it.name },
                            notes = current.notes,
                            paymentMethod = paymentMethod,
                            status = OrderStatus.CONFIRMED.name
                        )
                    )
                }
                clearCart()
                val mockLogistics = logisticsRepository.getMockLogistics(transactionId)
                _activeOrderLogistics.value = mockLogistics
                startCountdown()
                startStatusPolling(transactionId)
            } finally {
                onComplete(transactionId)
            }
        }
    }

    fun resetOrder() {
        _orderState.value = OrderState()
    }

    private fun startStatusPolling(orderId: String) {
        statusPollingJob?.cancel()
        statusPollingJob = viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(5000)
                try {
                    val status = orderRepository.getOrderStatus(orderId)
                    when (status) {
                        "COMPLETED" -> {
                            _activeOrderLogistics.update { it?.copy(status = OrderLogisticsStatus.DELIVERED) }
                            kotlinx.coroutines.delay(2000)
                            stopCountdown()
                            break
                        }
                        "PROCESSING" -> {
                            _activeOrderLogistics.update { it?.copy(status = OrderLogisticsStatus.IN_TRANSIT) }
                        }
                        "PENDING" -> {
                            _activeOrderLogistics.update { it?.copy(status = OrderLogisticsStatus.PREPARING) }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("ORDER", "Polling error: ${e.message}")
                }
            }
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            orderDao.deleteAllOrders()
        }
    }

    override fun onCleared() {
        countdownJob?.cancel()
        statusPollingJob?.cancel()
        super.onCleared()
    }
}
