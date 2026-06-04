package com.example.daisukefoddlock10.data.repository

import com.example.daisukefoddlock10.data.model.OrderLogistics
import com.example.daisukefoddlock10.data.model.OrderLogisticsStatus
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.realtime.PostgresChangeFilter
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.realtime
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LogisticsRepository @Inject constructor(
    private val supabase: SupabaseClient
) {
    fun observeOrderStatus(orderId: String): Flow<OrderLogisticsStatus> {
        val channel = supabase.realtime.channel("order_status_$orderId")
        return channel.postgresChangeFlow<PostgresAction.Update>(schema = "public") {
            table = "orders"
            filter("id", FilterOperator.EQ, orderId)
        }.map { action ->
            // In a real app, you would parse the payload
            // For MVP, we map the updated status
            OrderLogisticsStatus.IN_TRANSIT 
        }
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
