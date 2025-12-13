package com.pausrq.cucharita

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.pausrq.cucharita.api.ApiClient
import com.pausrq.cucharita.controllers.RecipeController
import com.pausrq.cucharita.models.Recipe
import com.pausrq.cucharita.storage.SessionManager
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val controller = RecipeController()
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RecipeAdapter
    private lateinit var addButton: FloatingActionButton
    private lateinit var favButton: FloatingActionButton
    private lateinit var galleryButton: FloatingActionButton
    private lateinit var searchBox: EditText
    private lateinit var sessionManager: SessionManager
    private var allRecipes: List<Recipe> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Verificar autenticación
        sessionManager = SessionManager(this)
        if (!sessionManager.isLoggedIn()) {
            goToLogin()
            return
        }

        setContentView(R.layout.activity_main)

        // Conectar UI
        recyclerView = findViewById(R.id.recyclerRecipes)
        addButton = findViewById(R.id.btnAdd)
        favButton = findViewById(R.id.btnFav)
        galleryButton = findViewById(R.id.btnGallery)
        searchBox = findViewById(R.id.etSearch)

        // Setup RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = RecipeAdapter(emptyList()) { recipe ->
            val intent = Intent(this, RecipeDetailActivity::class.java)
            intent.putExtra("recipeId", recipe.getId())
            startActivity(intent)
        }
        recyclerView.adapter = adapter

        // Cargar recetas
        loadRecipes()

        // Botones
        addButton.setOnClickListener {
            startActivity(Intent(this, AddRecipeActivity::class.java))
        }

        favButton.setOnClickListener {
            startActivity(Intent(this, FavoritesActivity::class.java))
        }

        galleryButton.setOnClickListener {
            startActivity(Intent(this, RecipeGalleryActivity::class.java))
        }

        // Búsqueda local (filtrado en memoria)
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                showLogoutDialog()
                true
            }
            R.id.action_refresh -> {
                loadRecipes()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun loadRecipes() {
        lifecycleScope.launch {
            try {
                val result = controller.getAllRecipes()
                result.onSuccess { recipes ->
                    allRecipes = recipes
                    adapter.updateData(recipes)

                    if (recipes.isEmpty()) {
                        Toast.makeText(
                            this@MainActivity,
                            "No hay recetas. ¡Agrega una!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                result.onFailure { error ->
                    Toast.makeText(
                        this@MainActivity,
                        "Error al cargar recetas: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@MainActivity,
                    "Error de conexión: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(this)
            .setTitle("Cerrar Sesión")
            .setMessage("¿Estás seguro que deseas cerrar sesión?")
            .setPositiveButton("Sí") { _, _ ->
                sessionManager.clearSession()
                ApiClient.clearSession()
                goToLogin()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun goToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onResume() {
        super.onResume()
        loadRecipes()
    }
}