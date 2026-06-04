package com.example.daisukefoddlock10.data.repository

import android.util.Log
import com.example.daisukefoddlock10.data.model.OrderLogistics
import com.example.daisukefoddlock10.data.model.OrderLogisticsStatus
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LogisticsRepository @Inject constructor(
    private val supabase: SupabaseClient
) {
    /**
     * Mengamati perubahan status pesanan secara real-time dari Supabase.
     * Saat merchant ubah status ke COMPLETED, customer akan mendapat notifikasi.
     */
    fun observeOrderStatus(orderId: String): Flow<OrderLogisticsStatus> = flow {
        val channelName = "order_status_$orderId"
        val channel = supabase.realtime.channel(channelName)

        // Emit status terbaru via Realtime saat merchant update pesanan
        val changeFlow = channel.postgresChangeFlow<PostgresAction.Update>(schema = "public") {
            table = "orders"
            filter("id", io.github.jan.supabase.postgrest.query.filter.FilterOperator.EQ, orderId)
        }.mapNotNull { action ->
            try {
                // Baca status baru dari payload Supabase
                val newRecord = action.record
                val statusStr = newRecord["status"]?.jsonPrimitive?.content
                Log.d("LogisticsRepo", "Order $orderId status updated to: $statusStr")
                when (statusStr) {
                    "COMPLETED" -> OrderLogisticsStatus.DELIVERED
                    "PROCESSING" -> OrderLogisticsStatus.IN_TRANSIT
                    "PENDING" -> OrderLogisticsStatus.PREPARING
                    else -> null
                }
            } catch (e: Exception) {
                Log.e("LogisticsRepo", "Failed to parse status update: ${e.message}")
                null
            }
        }

        // Wajib subscribe agar channel mulai mendengarkan!
        channel.subscribe()
        Log.d("LogisticsRepo", "Subscribed to realtime channel: $channelName")

        emitAll(changeFlow)
    }

    // Mock implementation for UNS Campus nodes
    fun getMockLogistics(orderId: String): OrderLogistics {
        return OrderLogistics(
            id = orderId,
            status = OrderLogisticsStatus.PREPARING,
            origin_name = "Kantin FMIPA",
            destination_name = "Lobi FATISDA",
            prep_time = 10,
            transit_time = 5
        )
    }
}
