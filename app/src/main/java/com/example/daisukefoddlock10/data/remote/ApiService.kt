package com.example.daisukefoddlock10.data.remote

import com.example.daisukefoddlock10.data.model.MidtransApiResponse
import com.example.daisukefoddlock10.data.model.MidtransOrderRequest
import com.example.daisukefoddlock10.data.model.MidtransOrderResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("api/orders")
    suspend fun createMidtransOrder(
        @Body request: MidtransOrderRequest
    ): MidtransApiResponse<MidtransOrderResponse>
}
