package com.pausrq.cucharita

import android.app.AlertDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.pausrq.cucharita.controllers.RecipeController
import com.pausrq.cucharita.models.Recipe
import com.pausrq.cucharita.utils.Util

class EditRecipeActivity : AppCompatActivity() {

    private val controller = RecipeController()
    private lateinit var searchInput: EditText
    private lateinit var titleInput: EditText
    private lateinit var descInput: EditText
    private lateinit var ingredientsInput: EditText
    private lateinit var stepsInput: EditText
    private lateinit var searchButton: Button
    private lateinit var saveButton: Button
    private var currentRecipe: Recipe? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_recipe)

        // connect layout elements
        searchInput = findViewById(R.id.editSearchName)
        searchButton = findViewById(R.id.btnSearch)
        titleInput = findViewById(R.id.editTitle)
        descInput = findViewById(R.id.editDescription)
        ingredientsInput = findViewById(R.id.editIngredients)
        stepsInput = findViewById(R.id.editSteps)
        saveButton = findViewById(R.id.btnSaveChanges)

        // get recipe name passed from other screen (optional)
        val recipeName = intent.getStringExtra("recipeName")
        currentRecipe = recipeName?.let { controller.searchRecipe(it) }

        // fill if recipe came from another screen
        currentRecipe?.let { recipe -> fillFields(recipe) }

        // search button logic
        searchButton.setOnClickListener {
            val name = searchInput.text.toString().trim()
            if (name.isEmpty()) {
                Toast.makeText(this, "Please enter a recipe name to search", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val found = controller.searchRecipe(name)
            if (found != null) {
                currentRecipe = found
                fillFields(found)
                Toast.makeText(this, "Recipe loaded successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Recipe not found", Toast.LENGTH_SHORT).show()
            }
        }

        // save changes button
        saveButton.setOnClickListener {
            if (titleInput.text.isNullOrBlank() || descInput.text.isNullOrBlank()) {
                Toast.makeText(this, getString(R.string.toast_empty_fields), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // show confirmation dialog
            AlertDialog.Builder(this)
                .setTitle(getString(R.string.dialog_confirm_update_title))
                .setMessage(getString(R.string.dialog_confirm_update))
                .setPositiveButton(getString(R.string.btn_yes)) { _, _ ->
                    saveChanges()
                }
                .setNegativeButton(getString(R.string.btn_cancel), null)
                .show()
        }
    }

    private fun fillFields(recipe: Recipe) {
        titleInput.setText(recipe.getTitle())
        descInput.setText(recipe.getDescription())
        ingredientsInput.setText(Util.listToText(recipe.getIngredients()))
        stepsInput.setText(Util.listToText(recipe.getSteps()))
    }

    private fun saveChanges() {
        val title = titleInput.text.toString().trim()
        val desc = descInput.text.toString().trim()
        val ingredients = Util.textToList(ingredientsInput.text.toString())
        val steps = Util.textToList(stepsInput.text.toString())

        currentRecipe?.let { recipe ->
            recipe.setTitle(title)
            recipe.setDescription(desc)
            recipe.setIngredients(ingredients)
            recipe.setSteps(steps)
        }

        Toast.makeText(this, getString(R.string.toast_recipe_updated), Toast.LENGTH_SHORT).show()
        finish()
    }
}
