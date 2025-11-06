package com.pausrq.cucharita.storage

import com.pausrq.cucharita.interfaces.IDataManager
import com.pausrq.cucharita.models.Recipe

class MemoryRecipeManager : IDataManager {

    private val recipeList = mutableListOf<Recipe>()

    override fun addRecipe(recipe: Recipe) {
        recipeList.add(recipe)
    }

    override fun getAllRecipes(): List<Recipe> {
        return recipeList
    }

    override fun findRecipeByName(name: String): Recipe? {
        return recipeList.find { it.getTitle().equals(name, ignoreCase = true) }
    }

    override fun toggleFavorite(name: String) {
        val recipe = findRecipeByName(name)
        recipe?.setFavorite(!recipe.isFavorite())
    }

    override fun getFavorites(): List<Recipe> {
        return recipeList.filter { it.isFavorite() }
    }

    // NEW: delete recipe by name
    fun deleteRecipeByName(name: String) {
        val iterator = recipeList.iterator()
        while (iterator.hasNext()) {
            val recipe = iterator.next()
            if (recipe.getTitle().equals(name, ignoreCase = true)) {
                iterator.remove()
                break
            }
        }
    }
}
