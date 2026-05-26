package com.example.daisukefoddlock10.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val foodName: String,
    val price: Double,
    val timestamp: Long = System.currentTimeMillis()
)
