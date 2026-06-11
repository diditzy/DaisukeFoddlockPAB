package com.example.daisukefoddlock10.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.daisukefoddlock10.data.model.UserSession
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_session")

@Singleton
class SessionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val SESSION_KEY = stringPreferencesKey("session_data")

    val userSession: Flow<UserSession?> = context.dataStore.data.map { preferences ->
        preferences[SESSION_KEY]?.let { jsonString ->
            try {
                Json.decodeFromString<UserSession>(jsonString)
            } catch (e: Exception) {
                null
            }
        }
    }

    suspend fun saveSession(session: UserSession) {
        context.dataStore.edit { preferences ->
            preferences[SESSION_KEY] = Json.encodeToString(UserSession.serializer(), session)
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            preferences.remove(SESSION_KEY)
        }
    }

    /**
     * Mengambil JWT token secara synchronous.
     * Digunakan oleh OkHttp AuthInterceptor yang berjalan di thread non-main.
     */
    fun getToken(): String? = runBlocking {
        userSession.firstOrNull()?.token
    }
}
