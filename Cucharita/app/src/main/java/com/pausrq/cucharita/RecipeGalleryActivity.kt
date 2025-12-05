package com.pausrq.cucharita
import com.pausrq.cucharita.adapters.GalleryAdapter


import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pausrq.cucharita.controllers.RecipeController

class RecipeGalleryActivity : AppCompatActivity() {

    private val controller = RecipeController()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_gallery)

        // setup RecyclerView
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerGallery)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // use the new adapter
        val adapter = GalleryAdapter(controller.getRecipes())
        recyclerView.adapter = adapter
    }
}
