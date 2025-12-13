// 📁 app/src/main/java/com/pausrq/cucharita/EditRecipeActivity.kt
package com.pausrq.cucharita

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.pausrq.cucharita.controllers.RecipeController
import com.pausrq.cucharita.models.Recipe
import com.pausrq.cucharita.utils.Util
import kotlinx.coroutines.launch

class EditRecipeActivity : AppCompatActivity() {

    private val controller = RecipeController()
    private lateinit var titleInput: EditText
    private lateinit var descInput: EditText
    private lateinit var ingredientsInput: EditText
    private lateinit var stepsInput: EditText
    private lateinit var saveButton: Button
    private var currentRecipe: Recipe? = null
    private var recipeId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_recipe)

        // Conectar elementos
        titleInput = findViewById(R.id.editTitle)
        descInput = findViewById(R.id.editDescription)
        ingredientsInput = findViewById(R.id.editIngredients)
        stepsInput = findViewById(R.id.editSteps)
        saveButton = findViewById(R.id.btnSaveChanges)

        // Obtener ID de la receta
        recipeId = intent.getStringExtra("recipeId")

        if (recipeId != null) {
            loadRecipe(recipeId!!)
        } else {
            Toast.makeText(this, "Error: No se proporcionó ID de receta", Toast.LENGTH_SHORT).show()
            finish()
        }

        // Guardar cambios
        saveButton.setOnClickListener {
            if (titleInput.text.isNullOrBlank() || descInput.text.isNullOrBlank()) {
                Toast.makeText(this, "Por favor completa título y descripción", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            AlertDialog.Builder(this)
                .setTitle("Confirmar Actualización")
                .setMessage("¿Deseas guardar los cambios?")
                .setPositiveButton("Sí") { _, _ ->
                    saveChanges()
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    private fun loadRecipe(id: String) {
        lifecycleScope.launch {
            try {
                val result = controller.getRecipeById(id)

                result.onSuccess { recipe ->
                    currentRecipe = recipe
                    fillFields(recipe)
                }

                result.onFailure { error ->
                    Toast.makeText(
                        this@EditRecipeActivity,
                        "Error al cargar receta: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@EditRecipeActivity,
                    "Error de conexión: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                finish()
            }
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

            saveButton.isEnabled = false
            saveButton.text = "Guardando..."

            lifecycleScope.launch {
                try {
                    val result = controller.updateRecipe(recipe)

                    result.onSuccess {
                        Toast.makeText(
                            this@EditRecipeActivity,
                            "Receta actualizada exitosamente",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    }

                    result.onFailure { error ->
                        Toast.makeText(
                            this@EditRecipeActivity,
                            "Error al actualizar: ${error.message}",
                            Toast.LENGTH_LONG
                        ).show()
                        saveButton.isEnabled = true
                        saveButton.text = "Guardar Cambios"
                    }
                } catch (e: Exception) {
                    Toast.makeText(
                        this@EditRecipeActivity,
                        "Error de conexión: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    saveButton.isEnabled = true
                    saveButton.text = "Guardar Cambios"
                }
            }
        }
    }
}