package com.pausrq.cucharita

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.pausrq.cucharita.controllers.RecipeController
import com.pausrq.cucharita.models.Recipe

class MainActivity : AppCompatActivity() {

    private val controller = RecipeController()
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RecipeAdapter
    private lateinit var addButton: FloatingActionButton
    private lateinit var favButton: FloatingActionButton
    private lateinit var galleryButton: FloatingActionButton
    private lateinit var searchBox: EditText
    private var allRecipes: List<Recipe> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // connect UI elements
        recyclerView = findViewById(R.id.recyclerRecipes)
        addButton = findViewById(R.id.btnAdd)
        favButton = findViewById(R.id.btnFav)
        galleryButton = findViewById(R.id.btnGallery)
        searchBox = findViewById(R.id.etSearch)

        // setup list of recipes
        recyclerView.layoutManager = LinearLayoutManager(this)
        allRecipes = controller.getRecipes()
        adapter = RecipeAdapter(allRecipes) { recipe ->
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

        // ✅ open gallery screen
        galleryButton.setOnClickListener {
            startActivity(Intent(this, RecipeGalleryActivity::class.java))
        }

        // search by name or description
        searchBox.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().lowercase()
                val filtered = allRecipes.filter {
                    it.getTitle().lowercase().contains(query) ||
                            it.getDescription().lowercase().contains(query)
                }
                adapter.updateData(filtered)
            }
        })
    }

    // refresh list when user comes back
    override fun onResume() {
        super.onResume()
        allRecipes = controller.getRecipes()
        adapter.updateData(allRecipes)
    }
}
