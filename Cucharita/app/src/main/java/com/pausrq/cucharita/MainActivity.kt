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
import com.pausrq.cucharita.adapters.RecipeApiAdapter
import com.pausrq.cucharita.api.ApiRepository
import com.pausrq.cucharita.api.RetrofitClient
import com.pausrq.cucharita.api.models.RecipeResponse
import com.pausrq.cucharita.utils.SessionManager
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RecipeApiAdapter
    private lateinit var addButton: FloatingActionButton
    private lateinit var publicRecipesButton: FloatingActionButton
    private lateinit var searchBox: EditText

    private lateinit var sessionManager: SessionManager
    private val apiRepository = ApiRepository()

    private var allRecipes: List<RecipeResponse> = listOf()
    private var isShowingPublicRecipes = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sessionManager = SessionManager(this)

        // Verificar si está logueado
        if (!sessionManager.isLoggedIn()) {
            goToLogin()
            return
        }

        // Configurar token en Retrofit
        RetrofitClient.setAuthToken(sessionManager.getToken())

        setContentView(R.layout.activity_main)

        // Conectar UI elements
        recyclerView = findViewById(R.id.recyclerRecipes)
        addButton = findViewById(R.id.btnAdd)
        publicRecipesButton = findViewById(R.id.btnPublicRecipes)
        searchBox = findViewById(R.id.etSearch)

        // Setup RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = RecipeApiAdapter(listOf()) { recipe ->
            // Click en receta - abrir detalle
            val intent = Intent(this, RecipeDetailApiActivity::class.java)
            intent.putExtra("recipeId", recipe.id)
            startActivity(intent)
        }
        recyclerView.adapter = adapter

        // Cargar MIS recetas inicialmente
        loadMyRecipes()

        // Botón agregar receta
        addButton.setOnClickListener {
            startActivity(Intent(this, AddRecipeApiActivity::class.java))
        }

        // Botón ver recetas públicas
        publicRecipesButton.setOnClickListener {
            if (isShowingPublicRecipes) {
                loadMyRecipes()
            } else {
                loadPublicRecipes()
            }
        }

        // Búsqueda
        searchBox.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().lowercase()
                val filtered = allRecipes.filter {
                    it.title.lowercase().contains(query) ||
                            it.description.lowercase().contains(query)
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
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun loadMyRecipes() {
        lifecycleScope.launch {
            try {
                val response = apiRepository.getMyRecipes()

                if (response.isSuccessful && response.body() != null) {
                    allRecipes = response.body()!!
                    adapter.updateData(allRecipes)
                    isShowingPublicRecipes = false

                    // Actualizar texto del botón
                    publicRecipesButton.contentDescription = "Ver recetas públicas"
                    title = "Mis Recetas"
                } else {
                    Toast.makeText(this@MainActivity, "Error al cargar recetas", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadPublicRecipes() {
        lifecycleScope.launch {
            try {
                val response = apiRepository.getAllPublicRecipes()

                if (response.isSuccessful && response.body() != null) {
                    allRecipes = response.body()!!
                    adapter.updateData(allRecipes)
                    isShowingPublicRecipes = true

                    // Actualizar texto del botón
                    publicRecipesButton.contentDescription = "Ver mis recetas"
                    title = "Recetas Públicas"
                } else {
                    Toast.makeText(this@MainActivity, "Error al cargar recetas públicas", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(this)
            .setTitle("Cerrar sesión")
            .setMessage("¿Estás seguro que quieres cerrar sesión?")
            .setPositiveButton("Sí") { _, _ ->
                performLogout()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun performLogout() {
        lifecycleScope.launch {
            try {
                apiRepository.logout()
            } catch (e: Exception) {
                // Continuar aunque falle la llamada al servidor
            } finally {
                // Limpiar sesión local
                sessionManager.clearSession()
                RetrofitClient.setAuthToken(null)
                goToLogin()
            }
        }
    }

    private fun goToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onResume() {
        super.onResume()
        // Recargar según el modo actual
        if (isShowingPublicRecipes) {
            loadPublicRecipes()
        } else {
            loadMyRecipes()
        }
    }
}