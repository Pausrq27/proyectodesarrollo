package com.pausrq.cucharita

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.pausrq.cucharita.R
import com.pausrq.cucharita.controllers.RecipeController
import com.pausrq.cucharita.utils.Util

class RecipeDetailActivity : AppCompatActivity() {

    private val controller = RecipeController() // manage recipes

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_detail)

        // connect layout views
        val titleView = findViewById<TextView>(R.id.tvTitle)
        val descView = findViewById<TextView>(R.id.tvDescription)
        val ingView = findViewById<TextView>(R.id.tvIngredients)
        val stepsView = findViewById<TextView>(R.id.tvSteps)
        val favBtn = findViewById<Button>(R.id.btnFavorite)

        // get recipe name sent from MainActivity
        val recipeName = intent.getStringExtra("recipeName")
        val recipe = recipeName?.let { controller.searchRecipe(it) }

        // show recipe data if found
        if (recipe != null) {
            titleView.text = recipe.getTitle()
            descView.text = recipe.getDescription()
            ingView.text = Util.listToText(recipe.getIngredients())
            stepsView.text = Util.listToText(recipe.getSteps())

            // set button text depending on favorite state
            favBtn.text = if (recipe.isFavorite()) getString(R.string.btn_remove_favorite)
            else getString(R.string.btn_favorite)

            // toggle favorite on click
            favBtn.setOnClickListener {
                controller.markAsFavorite(recipe.getTitle())
                favBtn.text = if (recipe.isFavorite()) getString(R.string.btn_remove_favorite)
                else getString(R.string.btn_favorite)
            }
        }
    }
}
