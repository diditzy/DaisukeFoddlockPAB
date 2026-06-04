package com.example.daisukefoddlock10.ui.screens

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.daisukefoddlock10.data.local.dao.OrderDao
import com.example.daisukefoddlock10.data.local.entity.OrderEntity
import com.example.daisukefoddlock10.data.model.*
import com.example.daisukefoddlock10.data.repository.LogisticsRepository
import com.example.daisukefoddlock10.data.repository.OrderRepository
import com.example.daisukefoddlock10.formatTimestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.util.UUID

@HiltViewModel
class SharedOrderViewModel @Inject constructor(
    private val orderDao: OrderDao,
    private val logisticsRepository: LogisticsRepository,
    private val orderRepository: OrderRepository
) : ViewModel() {
    private val _orderState = MutableStateFlow(OrderState())
    val orderState: StateFlow<OrderState> = _orderState.asStateFlow()

    private val _activeOrderLogistics = MutableStateFlow<OrderLogistics?>(null)
    val activeOrderLogistics: StateFlow<OrderLogistics?> = _activeOrderLogistics.asStateFlow()

    private val _remainingMinutes = MutableStateFlow<Int>(20)
    val remainingMinutes: StateFlow<Int> = _remainingMinutes.asStateFlow()

    private var countdownJob: kotlinx.coroutines.Job? = null
    private var statusPollingJob: kotlinx.coroutines.Job? = null

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
        // Hapus banner estimasi dari layar customer
        _activeOrderLogistics.value = null
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

    fun confirmOrder(paymentMethod: String, onComplete: (String) -> Unit) {
        val current = _orderState.value
        if (current.cartItems.isEmpty()) return

        viewModelScope.launch {
            var transactionId = ""
            try {
                // 1. Konversi CartItem ke OrderItemData (tanpa imageRes agar bisa dikirim ke Supabase)
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

                // 2. Kirim ke Supabase dengan timeout agar tidak loading selamanya
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
                    Log.d("ORDER", "Successfully placed order in Supabase with id: $transactionId")
                } else {
                    val exception = result?.exceptionOrNull()
                    Log.w("ORDER", "Supabase order failed or timed out: ${exception?.message}. Falling back to local ID.")
                    transactionId = UUID.randomUUID().toString().take(8).uppercase()
                }
            } catch (e: Exception) {
                Log.e("ORDER", "Error during Supabase order request: ${e.message}. Falling back to local ID.")
                transactionId = UUID.randomUUID().toString().take(8).uppercase()
            }

            try {
                // 2. Simpan ke Room Database (untuk history lokal)
                current.cartItems.forEach { item ->
                    orderDao.insertOrder(
                        OrderEntity(
                            transactionId = transactionId,
                            foodName = item.food.name,
                            quantity = item.quantity,
                            price = item.itemTotalPrice.toDouble(), // Simple mapping
                            size = item.size.name,
                            toppings = item.toppings.joinToString(",") { it.name },
                            notes = current.notes,
                            paymentMethod = paymentMethod,
                            status = OrderStatus.CONFIRMED.name
                        )
                    )
                }

                // 3. Bersihkan keranjang
                clearCart()
                
                // 4. Inisialisasi Logistik
                val mockLogistics = logisticsRepository.getMockLogistics(transactionId)
                _activeOrderLogistics.value = mockLogistics
                startCountdown()
                
                // 4. Mulai polling status pesanan dari Supabase setiap 5 detik
                //    Ini lebih andal dari Realtime yang butuh konfigurasi tambahan
                startStatusPolling(transactionId)
            } catch (e: Exception) {
                Log.e("ORDER", "Local database or logistics failed", e)
            } finally {
                // Selalu panggil onComplete agar loading spinner hilang dan navigasi lanjut
                onComplete(transactionId)
            }
        }
    }

    fun resetOrder() {
        // JANGAN cancel statusPollingJob di sini!
        // Polling harus tetap jalan sampai merchant konfirmasi COMPLETED
        _orderState.value = OrderState()
    }

    /**
     * Poll status pesanan dari Supabase setiap 5 detik.
     * Saat merchant ubah status ke COMPLETED → banner customer hilang otomatis.
     * Tidak bergantung pada Supabase Realtime sehingga 100% andal.
     */
    private fun startStatusPolling(orderId: String) {
        statusPollingJob?.cancel()
        statusPollingJob = viewModelScope.launch {
            Log.d("ORDER", "Start polling status for order: $orderId")
            while (true) {
                kotlinx.coroutines.delay(5000) // Cek setiap 5 detik
                try {
                    val status = orderRepository.getOrderStatus(orderId)
                    Log.d("ORDER", "Polled status: $status for order $orderId")
                    when (status) {
                        "COMPLETED" -> {
                            Log.d("ORDER", "Order COMPLETED → stopping countdown & clearing banner")
                            _activeOrderLogistics.update { it?.copy(status = OrderLogisticsStatus.DELIVERED) }
                            kotlinx.coroutines.delay(2000) // Tampilkan "Selesai" 2 detik dulu
                            stopCountdown() // Hapus banner
                            break // Stop polling
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
