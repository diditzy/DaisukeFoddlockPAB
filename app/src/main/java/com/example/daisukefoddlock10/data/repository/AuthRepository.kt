package com.example.daisukefoddlock10.data.repository

import com.example.daisukefoddlock10.data.model.UserRole
import com.example.daisukefoddlock10.data.model.UserSession
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun signIn(email: String, password: String): Result<UserSession>
    suspend fun signOut(): Result<Unit>
    suspend fun getCurrentSession(): UserSession?
    fun observeSession(): Flow<UserSession?>
}
