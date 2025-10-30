package com.pausrq.cucharita

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.pausrq.cucharita.controllers.RecipeController
import com.pausrq.cucharita.models.Recipe
import com.pausrq.cucharita.AddRecipeActivity
import com.pausrq.cucharita.FavoritesActivity
import com.pausrq.cucharita.RecipeDetailActivity

class MainActivity : AppCompatActivity() {

    private val controller = RecipeController()
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RecipeAdapter
    private lateinit var addButton: FloatingActionButton
    private lateinit var favButton: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // connect UI elements
        recyclerView = findViewById(R.id.recyclerRecipes)
        addButton = findViewById(R.id.btnAdd)
        favButton = findViewById(R.id.btnFav)

        // setup list of recipes
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = RecipeAdapter(controller.getRecipes()) { recipe ->
            // open recipe detail
            val intent = Intent(this, RecipeDetailActivity::class.java)
            intent.putExtra("recipeName", recipe.getTitle())
            startActivity(intent)
        }
        recyclerView.adapter = adapter

        // open add recipe screen
        addButton.setOnClickListener {
            startActivity(Intent(this, AddRecipeActivity::class.java))
        }

        // open favorites screen
        favButton.setOnClickListener {
            startActivity(Intent(this, FavoritesActivity::class.java))
        }
    }

    // refresh list when user comes back
    override fun onResume() {
        super.onResume()
        adapter.updateData(controller.getRecipes())
    }
}
