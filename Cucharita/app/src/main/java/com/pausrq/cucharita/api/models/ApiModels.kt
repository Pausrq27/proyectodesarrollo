package com.pausrq.cucharita.api.models

import com.google.gson.annotations.SerializedName


data class RegisterRequest(
    val email: String,
    val password: String,
    val username: String? = null,
    val fullName: String? = null
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class CreateRecipeRequest(
    val title: String,
    val description: String,
    val ingredients: List<String>,
    val steps: List<String>,
    val imageUrl: String? = null,
    val isFavorite: Boolean = false
)

data class UpdateRecipeRequest(
    val title: String,
    val description: String,
    val ingredients: List<String>,
    val steps: List<String>,
    val imageUrl: String? = null,
    val isFavorite: Boolean = false
)

// ============================================
// MODELOS DE RESPONSE
// ============================================

data class AuthResponse(
    val message: String,
    val user: User,
    val session: Session
)

data class User(
    val id: String,
    val email: String,
    @SerializedName("user_metadata")
    val userMetadata: UserMetadata?
)

data class UserMetadata(
    val username: String?,
    @SerializedName("full_name")
    val fullName: String?
)

data class Session(
    @SerializedName("access_token")
    val accessToken: String,
    @SerializedName("refresh_token")
    val refreshToken: String,
    @SerializedName("expires_in")
    val expiresIn: Int
)

data class RecipeResponse(
    val id: Int,
    @SerializedName("user_id")
    val userId: String,
    val title: String,
    val description: String,
    val ingredients: List<String>,
    val steps: List<String>,
    @SerializedName("image_url")
    val imageUrl: String?,
    @SerializedName("is_favorite")
    val isFavorite: Boolean,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String,
    val profiles: Profile?
)

data class Profile(
    val username: String?,
    @SerializedName("full_name")
    val fullName: String?,
    @SerializedName("avatar_url")
    val avatarUrl: String?
)

data class MessageResponse(
    val message: String
)

data class ErrorResponse(
    val error: String
)