package com.pausrq.cucharita.interfaces
import com.pausrq.cucharita.models.Recipe

interface IDataManager {
    fun addRecipe(recipe: Recipe)
    fun getAllRecipes(): List<Recipe>
    fun findRecipeByName(name: String): Recipe?
    fun toggleFavorite(name: String)
    fun getFavorites(): List<Recipe>
}