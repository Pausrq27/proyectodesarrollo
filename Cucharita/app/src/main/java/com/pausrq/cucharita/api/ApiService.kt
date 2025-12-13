package com.pausrq.cucharita.api

import com.pausrq.cucharita.api.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ========== AUTH ENDPOINTS ==========

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("api/auth/logout")
    suspend fun logout(): Response<Map<String, String>>

    @GET("api/auth/me")
    suspend fun getCurrentUser(): Response<Map<String, User>>

    // ========== RECIPE ENDPOINTS ==========

    @GET("api/recipes")
    suspend fun getAllRecipes(): Response<RecipeResponse>

    @GET("api/recipes/my-recipes")
    suspend fun getMyRecipes(): Response<RecipeResponse>

    @GET("api/recipes/favorites")
    suspend fun getFavorites(): Response<RecipeResponse>

    @GET("api/recipes/search")
    suspend fun searchRecipes(@Query("query") query: String): Response<RecipeResponse>

    @GET("api/recipes/{id}")
    suspend fun getRecipeById(@Path("id") id: String): Response<RecipeResponse>

    @POST("api/recipes")
    suspend fun createRecipe(@Body request: RecipeRequest): Response<RecipeResponse>

    @PUT("api/recipes/{id}")
    suspend fun updateRecipe(
        @Path("id") id: String,
        @Body request: RecipeRequest
    ): Response<RecipeResponse>

    @PATCH("api/recipes/{id}/favorite")
    suspend fun toggleFavorite(@Path("id") id: String): Response<RecipeResponse>

    @DELETE("api/recipes/{id}")
    suspend fun deleteRecipe(@Path("id") id: String): Response<Map<String, String>>

    @Multipart
    @POST("api/recipes/{id}/image")
    suspend fun uploadImage(
        @Path("id") id: String,
        @Part image: MultipartBody.Part
    ): Response<RecipeResponse>
}