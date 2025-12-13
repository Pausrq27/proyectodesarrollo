package com.pausrq.cucharita

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pausrq.cucharita.adapters.GalleryAdapter
import com.pausrq.cucharita.controllers.RecipeController
import kotlinx.coroutines.launch

class RecipeGalleryActivity : AppCompatActivity() {

    private val controller = RecipeController()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_gallery)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerGallery)
        recyclerView.layoutManager = LinearLayoutManager(this)

        loadGallery(recyclerView)
    }

    private fun loadGallery(recyclerView: RecyclerView) {
        lifecycleScope.launch {
            try {
                val result = controller.getAllRecipes()

                result.onSuccess { recipes ->
                    val adapter = GalleryAdapter(recipes) { recipe ->
                        val intent = Intent(this@RecipeGalleryActivity, RecipeDetailActivity::class.java)
                        intent.putExtra("recipeId", recipe.getId())
                        startActivity(intent)
                    }
                    recyclerView.adapter = adapter

                    if (recipes.isEmpty()) {
                        Toast.makeText(
                            this@RecipeGalleryActivity,
                            "No hay recetas para mostrar",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                result.onFailure { error ->
                    Toast.makeText(
                        this@RecipeGalleryActivity,
                        "Error al cargar galería: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@RecipeGalleryActivity,
                    "Error de conexión: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerGallery)
        loadGallery(recyclerView)
    }