package com.example.daisukefoddlock10.ui.screens.merchant

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.daisukefoddlock10.data.model.OrderResponse
import com.example.daisukefoddlock10.util.NotificationHelper
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.PostgresAction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class MerchantUiState {
    object Loading : MerchantUiState()
    data class Success(val orders: List<OrderResponse>) : MerchantUiState()
    data class Error(val message: String) : MerchantUiState()
}

@HiltViewModel
class MerchantViewModel @Inject constructor(
    private val postgrest: Postgrest,
    private val realtime: Realtime,
    private val notificationHelper: NotificationHelper
) : ViewModel() {

    private val _uiState = MutableStateFlow<MerchantUiState>(MerchantUiState.Loading)
    val uiState: StateFlow<MerchantUiState> = _uiState.asStateFlow()

    private var lastOrderCount = -1
    private var pollingJob: Job? = null

    init {
        loadOrders()
        startRealtimeListener()
    }

    // Muat pesanan dari Supabase via REST (lebih handal dari Realtime untuk initial load)
    fun loadOrders() {
        viewModelScope.launch {
            try {
                _uiState.value = MerchantUiState.Loading
                val orders = postgrest.from("orders")
                    .select()
                    .decodeList<OrderResponse>()
                Log.d("MerchantVM", "Loaded ${orders.size} orders from Supabase")
                _uiState.value = MerchantUiState.Success(orders)
                lastOrderCount = orders.size
            } catch (e: Exception) {
                Log.e("MerchantVM", "Failed to load orders: ${e.message}", e)
                _uiState.value = MerchantUiState.Error("Gagal memuat pesanan: ${e.message}")
            }
        }
    }

    // Dengarkan perubahan realtime — jika ada INSERT/UPDATE baru, reload data
    private fun startRealtimeListener() {
        viewModelScope.launch {
            try {
                val channel = realtime.channel("merchant-orders-listener")
                channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                    table = "orders"
                }.catch { e ->
                    Log.e("MerchantVM", "Realtime error: ${e.message}")
                }.collect { action ->
                    Log.d("MerchantVM", "Realtime change detected: ${action::class.simpleName}")
                    // Saat ada perubahan, reload data via REST
                    reloadOrdersSilently()
                }
                channel.subscribe()
            } catch (e: Exception) {
                Log.e("MerchantVM", "Failed to start realtime listener: ${e.message}")
                // Fallback: gunakan polling setiap 10 detik
                startPolling()
            }
        }
    }

    // Polling fallback: reload setiap 10 detik tanpa menampilkan loading spinner
    private fun startPolling() {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (true) {
                delay(10_000)
                reloadOrdersSilently()
            }
        }
    }

    // Reload tanpa mengganti UI state ke Loading (agar tidak flicker)
    private suspend fun reloadOrdersSilently() {
        try {
            val orders = postgrest.from("orders")
                .select()
                .decodeList<OrderResponse>()
            val currentCount = orders.size
            if (lastOrderCount != -1 && currentCount > lastOrderCount) {
                notificationHelper.showOrderNotification(
                    "Pesanan Baru!",
                    "Ada ${currentCount - lastOrderCount} pesanan baru masuk."
                )
            }
            lastOrderCount = currentCount
            _uiState.value = MerchantUiState.Success(orders)
        } catch (e: Exception) {
            Log.e("MerchantVM", "Failed to silently reload orders: ${e.message}")
        }
    }

    fun updateOrderStatus(orderId: String, newStatus: String) {
        viewModelScope.launch {
            try {
                postgrest.from("orders").update(
                    mapOf("status" to newStatus)
                ) {
                    filter {
                        eq("id", orderId)
                    }
                }
                // Reload setelah update status
                reloadOrdersSilently()
            } catch (e: Exception) {
                Log.e("MerchantVM", "Failed to update order status: ${e.message}")
            }
        }
    }

    override fun onCleared() {
        pollingJob?.cancel()
        super.onCleared()
    }
}
