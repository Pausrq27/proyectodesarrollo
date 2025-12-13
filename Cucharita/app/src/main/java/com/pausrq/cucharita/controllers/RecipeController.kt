package com.pausrq.cucharita.controllers

import android.graphics.Bitmap
import com.pausrq.cucharita.api.ApiClient
import com.pausrq.cucharita.api.RecipeData
import com.pausrq.cucharita.api.RecipeRequest
import com.pausrq.cucharita.models.Recipe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream

class RecipeController {

    private val apiService = ApiClient.getApiService()

    // Convertir RecipeData (API) a Recipe (modelo local)
    private fun toRecipe(data: RecipeData): Recipe {
        return Recipe(
            id = data.id,
            userId = data.user_id,
            title = data.title,
            description = data.description,
            ingredients = data.ingredients,
            steps = data.steps,
            imageUrl = data.image_url,
            isFavorite = data.is_favorite
        )
    }

    // ========== OBTENER RECETAS ==========

    suspend fun getAllRecipes(): Result<List<Recipe>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getAllRecipes()
            if (response.isSuccessful && response.body() != null) {
                val recipes = response.body()!!.recipes?.map { toRecipe(it) } ?: emptyList()
                Result.success(recipes)
            } else {
                Result.failure(Exception("Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMyRecipes(): Result<List<Recipe>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getMyRecipes()
            if (response.isSuccessful && response.body() != null) {
                val recipes = response.body()!!.recipes?.map { toRecipe(it) } ?: emptyList()
                Result.success(recipes)
            } else {
                Result.failure(Exception("Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getFavorites(): Result<List<Recipe>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getFavorites()
            if (response.isSuccessful && response.body() != null) {
                val recipes = response.body()!!.recipes?.map { toRecipe(it) } ?: emptyList()
                Result.success(recipes)
            } else {
                Result.failure(Exception("Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchRecipe(query: String): Result<List<Recipe>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.searchRecipes(query)
            if (response.isSuccessful && response.body() != null) {
                val recipes = response.body()!!.recipes?.map { toRecipe(it) } ?: emptyList()
                Result.success(recipes)
            } else {
                Result.failure(Exception("Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRecipeById(id: String): Result<Recipe> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getRecipeById(id)
            if (response.isSuccessful && response.body()?.recipe != null) {
                val recipe = toRecipe(response.body()!!.recipe!!)
                Result.success(recipe)
            } else {
                Result.failure(Exception("Recipe not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ========== CREAR Y ACTUALIZAR ==========

    suspend fun createRecipe(recipe: Recipe): Result<Recipe> = withContext(Dispatchers.IO) {
        try {
            val request = RecipeRequest(
                title = recipe.getTitle(),
                description = recipe.getDescription(),
                ingredients = recipe.getIngredients(),
                steps = recipe.getSteps()
            )

            val response = apiService.createRecipe(request)
            if (response.isSuccessful && response.body()?.recipe != null) {
                val createdRecipe = toRecipe(response.body()!!.recipe!!)
                Result.success(createdRecipe)
            } else {
                Result.failure(Exception("Error creating recipe: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateRecipe(recipe: Recipe): Result<Recipe> = withContext(Dispatchers.IO) {
        try {
            val request = RecipeRequest(
                title = recipe.getTitle(),
                description = recipe.getDescription(),
                ingredients = recipe.getIngredients(),
                steps = recipe.getSteps(),
                is_favorite = recipe.isFavorite()
            )

            val response = apiService.updateRecipe(recipe.getId(), request)
            if (response.isSuccessful && response.body()?.recipe != null) {
                val updatedRecipe = toRecipe(response.body()!!.recipe!!)
                Result.success(updatedRecipe)
            } else {
                Result.failure(Exception("Error updating recipe: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun toggleFavorite(recipeId: String): Result<Recipe> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.toggleFavorite(recipeId)
            if (response.isSuccessful && response.body()?.recipe != null) {
                val recipe = toRecipe(response.body()!!.recipe!!)
                Result.success(recipe)
            } else {
                Result.failure(Exception("Error toggling favorite: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ========== ELIMINAR ==========

    suspend fun deleteRecipe(recipeId: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.deleteRecipe(recipeId)
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(Exception("Error deleting recipe: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ========== SUBIR IMAGEN ==========

    suspend fun uploadImage(recipeId: String, bitmap: Bitmap): Result<Recipe> = withContext(Dispatchers.IO) {
        try {
            // Convertir Bitmap a bytes
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
            val byteArray = stream.toByteArray()

            // Crear multipart body
            val requestBody = byteArray.toRequestBody("image/jpeg".toMediaTypeOrNull())
            val imagePart = MultipartBody.Part.createFormData("image", "recipe_image.jpg", requestBody)

            // Subir imagen
            val response = apiService.uploadImage(recipeId, imagePart)
            if (response.isSuccessful && response.body()?.recipe != null) {
                val recipe = toRecipe(response.body()!!.recipe!!)
                Result.success(recipe)
            } else {
                Result.failure(Exception("Error uploading image: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}