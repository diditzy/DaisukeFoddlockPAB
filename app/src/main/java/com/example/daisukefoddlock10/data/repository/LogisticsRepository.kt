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
    fun observeOrderStatus(orderId: String): Flow<OrderLogisticsStatus> = flow {
        val channelName = "order_status_$orderId"
        val channel = supabase.realtime.channel(channelName)

        val changeFlow = channel.postgresChangeFlow<PostgresAction.Update>(schema = "public") {
            table = "orders"
            filter("id", io.github.jan.supabase.postgrest.query.filter.FilterOperator.EQ, orderId)
        }.mapNotNull { action ->
            try {
                val newRecord = action.record
                val statusStr = newRecord["status"]?.jsonPrimitive?.content
                when (statusStr) {
                    "COMPLETED" -> OrderLogisticsStatus.DELIVERED
                    "PROCESSING" -> OrderLogisticsStatus.IN_TRANSIT
                    "PENDING" -> OrderLogisticsStatus.PREPARING
                    else -> null
                }
            } catch (e: Exception) {
                null
            }
        }

        channel.subscribe()
        emitAll(changeFlow)
    }

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
