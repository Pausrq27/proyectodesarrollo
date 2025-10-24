package com.pausrq.cucharita.storage
import com.pausrq.cucharita.models.Recipe
import com.pausrq.cucharita.interfaces.IDataManager


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
}