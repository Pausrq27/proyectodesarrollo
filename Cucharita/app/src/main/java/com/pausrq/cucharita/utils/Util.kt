package com.pausrq.cucharita.utils

object Util {

    fun capitalizeText(text: String): String {
        return text.trim().replaceFirstChar { it.uppercase() }
    }

    fun validateRecipeData(title: String, description: String): Boolean {
        return title.isNotBlank() && description.isNotBlank()
    }

    fun listToText(items: List<String>): String {
        return items.joinToString(separator = ", ")
    }

    fun textToList(text: String): List<String> {
        return text.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    }
}