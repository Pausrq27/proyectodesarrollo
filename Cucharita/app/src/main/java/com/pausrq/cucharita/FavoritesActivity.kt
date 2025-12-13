package com.pausrq.cucharita

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pausrq.cucharita.controllers.RecipeController
import kotlinx.coroutines.launch

class FavoritesActivity : AppCompatActivity() {

    private val controller = RecipeController()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorites)

        val recycler = findViewById<RecyclerView>(R.id.recyclerFavorites)
        recycler.layoutManager = LinearLayoutManager(this)

        loadFavorites(recycler)
    }

    private fun loadFavorites(recycler: RecyclerView) {
        lifecycleScope.launch {
            try {
                val result = controller.getFavorites()

                result.onSuccess { recipes ->
                    val adapter = RecipeAdapter(recipes) { recipe ->
                        val intent = Intent(this@FavoritesActivity, RecipeDetailActivity::class.java)
                        intent.putExtra("recipeId", recipe.getId())
                        startActivity(intent)
                    }
                    recycler.adapter = adapter

                    if (recipes.isEmpty()) {
                        Toast.makeText(
                            this@FavoritesActivity,
                            "No tienes recetas favoritas",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                result.onFailure { error ->
                    Toast.makeText(
                        this@FavoritesActivity,
                        "Error al cargar favoritos: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@FavoritesActivity,
                    "Error de conexión: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val recycler = findViewById<RecyclerView>(R.id.recyclerFavorites)
        loadFavorites(recycler)
    }
}