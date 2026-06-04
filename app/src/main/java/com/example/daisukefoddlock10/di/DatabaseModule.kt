package com.example.daisukefoddlock10.di

import android.content.Context
import androidx.room.Room
import com.example.daisukefoddlock10.data.local.AppDatabase
import com.example.daisukefoddlock10.data.local.dao.OrderDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "daisuke_foodlocks_db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideOrderDao(database: AppDatabase): OrderDao {
        return database.orderDao()
    }
}
