package com.pausrq.cucharita

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.pausrq.cucharita.R
import com.pausrq.cucharita.controllers.RecipeController
import com.pausrq.cucharita.models.Recipe
import com.pausrq.cucharita.utils.Util

class AddRecipeActivity : AppCompatActivity() {

    private val controller = RecipeController() // control access to recipes

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_recipe)

        // link UI elements
        val titleField = findViewById<EditText>(R.id.editTitle)
        val descField = findViewById<EditText>(R.id.editDescription)
        val ingredientsField = findViewById<EditText>(R.id.editIngredients)
        val stepsField = findViewById<EditText>(R.id.editSteps)
        val saveBtn = findViewById<Button>(R.id.btnSave)

        // save recipe when button pressed
        saveBtn.setOnClickListener {
            val title = titleField.text.toString()
            val desc = descField.text.toString()

            // validate that title and description are not empty
            if (!Util.validateRecipeData(title, desc)) {
                Toast.makeText(this, getString(R.string.toast_empty_fields), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // convert text to lists
            val ingredients = Util.textToList(ingredientsField.text.toString())
            val steps = Util.textToList(stepsField.text.toString())

            // create recipe object
            val recipe = Recipe(title, desc, ingredients, steps)

            // add recipe to memory
            controller.addNewRecipe(recipe)
            Toast.makeText(this, getString(R.string.toast_recipe_added), Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
