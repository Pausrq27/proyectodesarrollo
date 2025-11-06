package com.pausrq.cucharita

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.pausrq.cucharita.controllers.RecipeController
import com.pausrq.cucharita.utils.Util

class RecipeDetailActivity : AppCompatActivity() {

    private val controller = RecipeController()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_detail)

        // connect layout views
        val titleView = findViewById<TextView>(R.id.tvTitle)
        val descView = findViewById<TextView>(R.id.tvDescription)
        val ingView = findViewById<TextView>(R.id.tvIngredients)
        val stepsView = findViewById<TextView>(R.id.tvSteps)
        val favBtn = findViewById<Button>(R.id.btnFavorite)
        val editBtn = findViewById<Button>(R.id.btnEdit)
        val deleteBtn = findViewById<Button>(R.id.btnDelete)

        // get recipe name sent from MainActivity
        val recipeName = intent.getStringExtra("recipeName")
        val recipe = recipeName?.let { controller.searchRecipe(it) }

        if (recipe != null) {
            titleView.text = recipe.getTitle()
            descView.text = recipe.getDescription()
            ingView.text = Util.listToText(recipe.getIngredients())
            stepsView.text = Util.listToText(recipe.getSteps())

            favBtn.text = if (recipe.isFavorite())
                getString(R.string.btn_remove_favorite)
            else
                getString(R.string.btn_favorite)

            favBtn.setOnClickListener {
                controller.markAsFavorite(recipe.getTitle())
                favBtn.text = if (recipe.isFavorite())
                    getString(R.string.btn_remove_favorite)
                else
                    getString(R.string.btn_favorite)
            }

            // open edit screen
            editBtn.setOnClickListener {
                val intent = Intent(this, EditRecipeActivity::class.java)
                intent.putExtra("recipeName", recipe.getTitle())
                startActivity(intent)
            }

            // confirm delete
            deleteBtn.setOnClickListener {
                AlertDialog.Builder(this)
                    .setTitle("Confirm Deletion")
                    .setMessage("Are you sure you want to delete this recipe?")
                    .setPositiveButton("Yes") { _, _ ->
                        controller.deleteRecipe(recipe.getTitle())
                        finish()
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }
    }
}
