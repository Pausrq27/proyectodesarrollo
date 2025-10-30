package com.pausrq.cucharita.controllers
import com.pausrq.cucharita.models.Recipe
import com.pausrq.cucharita.storage.MemoryRecipeManager
class RecipeController {

    private val memoryManager = MemoryRecipeManager()

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
}