package com.example.daisukefoddlock10.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val transactionId: String,
    val foodName: String,
    val quantity: Int = 1,
    val price: Double,
    val size: String = "REGULAR",
    val toppings: String = "",
    val notes: String = "",
    val paymentMethod: String = "Tunai",
    val status: String = "COMPLETED",
    val timestamp: Long = System.currentTimeMillis()
)
