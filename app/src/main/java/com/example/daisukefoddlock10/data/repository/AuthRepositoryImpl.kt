package com.example.daisukefoddlock10.data.repository

import android.util.Log
import com.example.daisukefoddlock10.data.local.SessionManager
import com.example.daisukefoddlock10.data.model.UserRole
import com.example.daisukefoddlock10.data.model.UserSession
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val auth: Auth,
    private val postgrest: Postgrest,
    private val sessionManager: SessionManager
) : AuthRepository {

    override suspend fun signIn(email: String, password: String): Result<UserSession> {
        return try {
            auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            
            val user = auth.currentUserOrNull() ?: throw Exception("User not found after sign in")
            Log.d("AuthRepo", "User signed in: ${user.id}")
            Log.d("AuthRepo", "User Metadata: ${user.userMetadata}")
            
            val roleStr = user.userMetadata?.get("role")?.jsonPrimitive?.contentOrNull ?: "CUSTOMER"
            Log.d("AuthRepo", "Detected Role String: $roleStr")
            
            val role = try { 
                UserRole.valueOf(roleStr.uppercase()) 
            } catch (e: Exception) { 
                Log.e("AuthRepo", "Invalid role: $roleStr, defaulting to CUSTOMER")
                UserRole.CUSTOMER 
            }
            
            Log.d("AuthRepo", "Final Role: $role")
            
            val session = UserSession(
                id = user.id,
                email = user.email ?: "",
                role = role
            )
            
            sessionManager.saveSession(session)
            Result.success(session)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signOut(): Result<Unit> {
        return try {
            auth.signOut()
            sessionManager.clearSession()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCurrentSession(): UserSession? {
        return sessionManager.userSession.firstOrNull()
    }

    override fun observeSession(): Flow<UserSession?> {
        return sessionManager.userSession
    }
}
