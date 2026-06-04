package com.example.daisukefoddlock10.di

import android.content.Context
import com.example.daisukefoddlock10.util.NotificationHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.realtime
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SupabaseModule {

    @Provides
    @Singleton
    fun provideSupabaseClient(): SupabaseClient {
        return createSupabaseClient(
            supabaseUrl = "https://cznfldmtttxlvaggpilp.supabase.co",
            // PENTING: Gunakan "anon public" key dari Supabase Dashboard > Settings > API
            // Key yang benar diawali dengan "eyJ..." bukan "sb_publishable_..."
            supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImN6bmZsZG10dHR4bHZhZ2dwaWxwIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODA1NzkzNTAsImV4cCI6MjA5NjE1NTM1MH0.WL4Af_0LMWvWvyZmsMHIBwL5iYpG-nFBujoxCoFZoMI"
        ) {
            install(Postgrest)
            install(Realtime)
            install(Auth)
        }
    }

    @Provides
    @Singleton
    fun provideSupabaseAuth(client: SupabaseClient): Auth {
        return client.auth
    }

    @Provides
    @Singleton
    fun provideSupabasePostgrest(client: SupabaseClient): Postgrest {
        return client.postgrest
    }

    @Provides
    @Singleton
    fun provideSupabaseRealtime(client: SupabaseClient): Realtime {
        return client.realtime
    }

    @Provides
    @Singleton
    fun provideNotificationHelper(@ApplicationContext context: Context): NotificationHelper {
        return NotificationHelper(context)
    }
}
