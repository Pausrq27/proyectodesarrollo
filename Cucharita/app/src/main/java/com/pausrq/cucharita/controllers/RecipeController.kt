package com.pausrq.cucharita.controllers

import com.pausrq.cucharita.models.Recipe
import com.pausrq.cucharita.storage.MemoryRecipeManager

class RecipeController {

    // ✅ Singleton reference
    private val memoryManager = MemoryRecipeManager

    fun addNewRecipe(recipe: Recipe) {
        memoryManager.addRecipe(recipe)
    }

    fun getRecipes(): List<Recipe> {
        return memoryManager.getAllRecipes()
    }

    fun searchRecipe(name: String): Recipe? {
        return memoryManager.findRecipeByName(name)
    }

    fun markAsFavorite(name: String) {
        memoryManager.toggleFavorite(name)
    }

    fun getFavoriteRecipes(): List<Recipe> {
        return memoryManager.getFavorites()
    }

    // NEW METHOD: delete a recipe by name
    fun deleteRecipe(name: String) {
        memoryManager.deleteRecipeByName(name)
    }
}
