// 📁 app/src/main/java/com/pausrq/cucharita/models/Recipe.kt
package com.pausrq.cucharita.models

import android.graphics.Bitmap

data class Recipe(
    private var id: String = "", // ⭐
    private var userId: String = "",
    private var title: String,
    private var description: String,
    private var ingredients: List<String>,
    private var steps: List<String>,
    private var imageUrl: String? = null,
    private var localImage: Bitmap? = null,
    private var isFavorite: Boolean = false
) {

    // ID
    fun getId(): String = id
    fun setId(value: String) { id = value }

    // User ID
    fun getUserId(): String = userId
    fun setUserId(value: String) { userId = value }

    // Title
    fun getTitle(): String = title
    fun setTitle(value: String) { title = value }

    // Description
    fun getDescription(): String = description
    fun setDescription(value: String) { description = value }

    // Ingredients
    fun getIngredients(): List<String> = ingredients
    fun setIngredients(list: List<String>) { ingredients = list }

    // Steps
    fun getSteps(): List<String> = steps
    fun setSteps(list: List<String>) { steps = list }

    // Image URL (desde API)
    fun getImageUrl(): String? = imageUrl
    fun setImageUrl(value: String?) { imageUrl = value }

    // Local Image (para preview)
    fun getLocalImage(): Bitmap? = localImage
    fun setLocalImage(value: Bitmap?) { localImage = value }

    // Favorite
    fun isFavorite(): Boolean = isFavorite
    fun setFavorite(value: Boolean) { isFavorite = value }
}