// 📁 app/src/main/java/com/pausrq/cucharita/RecipeDetailActivity.kt
package com.pausrq.cucharita

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.pausrq.cucharita.controllers.RecipeController
import com.pausrq.cucharita.models.Recipe
import com.pausrq.cucharita.utils.Util
import kotlinx.coroutines.launch

class RecipeDetailActivity : AppCompatActivity() {

    private val controller = RecipeController()
    private var currentRecipe: Recipe? = null
    private var recipeId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_detail)

        // Obtener ID
        recipeId = intent.getStringExtra("recipeId")

        if (recipeId != null) {
            loadRecipe(recipeId!!)
        } else {
            Toast.makeText(this, "Error: No se proporcionó ID de receta", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun loadRecipe(id: String) {
        lifecycleScope.launch {
            try {
                val result = controller.getRecipeById(id)

                result.onSuccess { recipe ->
                    currentRecipe = recipe
                    displayRecipe(recipe)
                }

                result.onFailure { error ->
                    Toast.makeText(
                        this@RecipeDetailActivity,
                        "Error al cargar receta: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@RecipeDetailActivity,
                    "Error de conexión: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                finish()
            }
        }
    }

    private fun displayRecipe(recipe: Recipe) {
        val titleView = findViewById<TextView>(R.id.tvTitle)
        val descView = findViewById<TextView>(R.id.tvDescription)
        val ingView = findViewById<TextView>(R.id.tvIngredients)
        val stepsView = findViewById<TextView>(R.id.tvSteps)
        val imageView = findViewById<ImageView>(R.id.recipeImage)
        val favBtn = findViewById<Button>(R.id.btnFavorite)
        val editBtn = findViewById<Button>(R.id.btnEdit)
        val deleteBtn = findViewById<Button>(R.id.btnDelete)

        titleView.text = recipe.getTitle()
        descView.text = recipe.getDescription()
        ingView.text = Util.listToText(recipe.getIngredients())
        stepsView.text = Util.listToText(recipe.getSteps())

        // Cargar imagen si existe
        if (!recipe.getImageUrl().isNullOrEmpty()) {
            Glide.with(this)
                .load(recipe.getImageUrl())
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background)
                .into(imageView)
            imageView.visibility = android.view.View.VISIBLE
        } else {
            imageView.visibility = android.view.View.GONE
        }

        // Botón favorito
        favBtn.text = if (recipe.isFavorite()) "Quitar de Favoritos" else "Agregar a Favoritos"
        favBtn.setOnClickListener {
            toggleFavorite()
        }

        // Botón editar
        editBtn.setOnClickListener {
            val intent = Intent(this, EditRecipeActivity::class.java)
            intent.putExtra("recipeId", recipe.getId())
            startActivity(intent)
        }

        // Botón eliminar
        deleteBtn.setOnClickListener {
            showDeleteDialog()
        }
    }

    private fun toggleFavorite() {
        lifecycleScope.launch {
            try {
                val result = controller.toggleFavorite(recipeId!!)

                result.onSuccess { updatedRecipe ->
                    currentRecipe = updatedRecipe
                    val favBtn = findViewById<Button>(R.id.btnFavorite)
                    favBtn.text = if (updatedRecipe.isFavorite())
                        "Quitar de Favoritos"
                    else
                        "Agregar a Favoritos"

                    Toast.makeText(
                        this@RecipeDetailActivity,
                        if (updatedRecipe.isFavorite()) "Agregado a favoritos" else "Quitado de favoritos",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                result.onFailure { error ->
                    Toast.makeText(
                        this@RecipeDetailActivity,
                        "Error: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@RecipeDetailActivity,
                    "Error de conexión: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun showDeleteDialog() {
        AlertDialog.Builder(this)
            .setTitle("Confirmar Eliminación")
            .setMessage("¿Estás seguro que deseas eliminar esta receta?")
            .setPositiveButton("Sí") { _, _ ->
                deleteRecipe()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun deleteRecipe() {
        lifecycleScope.launch {
            try {
                val result = controller.deleteRecipe(recipeId!!)

                result.onSuccess {
                    Toast.makeText(
                        this@RecipeDetailActivity,
                        "Receta eliminada",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }

                result.onFailure { error ->
                    Toast.makeText(
                        this@RecipeDetailActivity,
                        "Error al eliminar: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@RecipeDetailActivity,
                    "Error de conexión: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Recargar receta por si fue editada
        recipeId?.let { loadRecipe(it) }
    }
}