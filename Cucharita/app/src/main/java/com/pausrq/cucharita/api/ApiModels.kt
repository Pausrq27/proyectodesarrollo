package com.pausrq.cucharita.api

// ==================== AUTH MODELS ====================

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val email: String,
    val password: String,
    val name: String? = null
)

data class AuthResponse(
    val message: String,
    val user: User,
    val session: Session? = null,
    val access_token: String? = null
)

data class User(
    val id: String,
    val email: String,
    val user_metadata: UserMetadata? = null
)

data class UserMetadata(
    val name: String? = null
)

data class Session(
    val access_token: String,
    val refresh_token: String
)

// ==================== RECIPE MODELS ====================

data class RecipeRequest(
    val title: String,
    val description: String,
    val ingredients: List<String>,
    val steps: List<String>,
    val is_favorite: Boolean? = null
)

data class RecipeResponse(
    val message: String? = null,
    val recipe: RecipeData? = null,
    val recipes: List<RecipeData>? = null
)

data class RecipeData(
    val id: String,
    val user_id: String,
    val title: String,
    val description: String,
    val ingredients: List<String>,
    val steps: List<String>,
    val image_url: String? = null,
    val is_favorite: Boolean,
    val created_at: String,
    val updated_at: String
)

// ==================== ERROR MODEL ====================

data class ErrorResponse(
    val error: String
)