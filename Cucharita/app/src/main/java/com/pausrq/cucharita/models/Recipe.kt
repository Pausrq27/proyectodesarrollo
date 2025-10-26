package com.pausrq.cucharita.models

data class Recipe(
    private var title: String,
    private var description: String,
    private var ingredients: List<String>,
    private var steps: List<String>,
    private var image: String? = null,
    private var isFavorite: Boolean = false
) {

    fun getTitle(): String = title
    fun setTitle(value: String) { title = value }

    fun getDescription(): String = description
    fun setDescription(value: String) { description = value }

    fun getIngredients(): List<String> = ingredients
    fun setIngredients(list: List<String>) { ingredients = list }

    fun getSteps(): List<String> = steps
    fun setSteps(list: List<String>) { steps = list }

    fun getImage(): String? = image
    fun setImage(value: String?) { image = value }

    fun isFavorite(): Boolean = isFavorite
    fun setFavorite(value: Boolean) { isFavorite = value }
}
