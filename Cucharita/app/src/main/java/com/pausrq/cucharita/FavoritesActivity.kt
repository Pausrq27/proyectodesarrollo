package com.pausrq.cucharita

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pausrq.cucharita.R
import com.pausrq.cucharita.RecipeAdapter
import com.pausrq.cucharita.controllers.RecipeController

class FavoritesActivity : AppCompatActivity() {

    private val controller = RecipeController() // control access to favorites

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorites)

        // setup list of favorite recipes
        val recycler = findViewById<RecyclerView>(R.id.recyclerFavorites)
        recycler.layoutManager = LinearLayoutManager(this)

        // use adapter to open recipe details on click
        val adapter = RecipeAdapter(controller.getFavoriteRecipes()) { recipe ->
            val intent = Intent(this, RecipeDetailActivity::class.java)
            intent.putExtra("recipeName", recipe.getTitle())
            startActivity(intent)
        }
        recycler.adapter = adapter
    }
}
