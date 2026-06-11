package com.example.daisukefoddlock10.data.model

import kotlinx.serialization.Serializable

@Serializable
data class UserSession(
    val id: String,
    val email: String,
    val role: UserRole,
    val token: String? = null  // JWT token untuk autentikasi ke backend Spring Boot
)
