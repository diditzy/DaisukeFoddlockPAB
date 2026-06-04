package com.example.daisukefoddlock10.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class NodeType { CANTEEN, DROPOFF }

@Serializable
enum class OrderLogisticsStatus { PENDING, PREPARING, IN_TRANSIT, DELIVERED, CANCELLED }

@Serializable
data class Node(
    val id: String,
    val name: String,
    val type: NodeType,
    val base_prep_time_m: Int
)

@Serializable
data class OrderLogistics(
    val id: String,
    val status: OrderLogisticsStatus,
    val origin_name: String,
    val destination_name: String,
    val prep_time: Int,
    val transit_time: Int,
    val buffer_time: Int = 5
) {
    val total_eta: Int get() = prep_time + transit_time + buffer_time
}
