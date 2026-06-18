package com.example.daisukefoddlock10.data.repository

import android.util.Log
import com.example.daisukefoddlock10.data.model.OrderRequest
import com.example.daisukefoddlock10.data.model.OrderResponse
import io.github.jan.supabase.postgrest.Postgrest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrderRepository @Inject constructor(
    private val postgrest: Postgrest
) {
    suspend fun placeOrder(order: OrderRequest): Result<OrderResponse> {
        return try {
            val response = postgrest.from("orders").insert(order) {
                select()
            }.decodeSingle<OrderResponse>()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getOrderStatus(orderId: String): String? {
        return try {
            val response = postgrest.from("orders")
                .select {
                    filter { eq("id", orderId) }
                }.decodeSingle<OrderResponse>()
            Log.d("OrderRepo", "Polled order $orderId → status: ${response.status}")
            response.status
        } catch (e: Exception) {
            Log.e("OrderRepo", "Failed to poll order status: ${e.message}")
            null
        }
    }
}
