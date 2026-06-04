package com.example.daisukefoddlock10.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.daisukefoddlock10.data.local.dao.OrderDao
import com.example.daisukefoddlock10.data.local.entity.OrderEntity

@Database(entities = [OrderEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun orderDao(): OrderDao
}
