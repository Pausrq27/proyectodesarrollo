package com.pausrq.cucharita

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.pausrq.cucharita.api.ApiRepository
import com.pausrq.cucharita.api.RetrofitClient
import com.pausrq.cucharita.utils.SessionManager
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var emailField: EditText
    private lateinit var passwordField: EditText
    private lateinit var loginButton: Button
    private lateinit var registerLink: TextView
    private lateinit var progressBar: ProgressBar

    private lateinit var sessionManager: SessionManager
    private val apiRepository = ApiRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sessionManager = SessionManager(this)

        // Si ya está logueado, ir directo a MainActivity
        if (sessionManager.isLoggedIn()) {
            goToMainActivity()
            return
        }

        setContentView(R.layout.activity_login)

        // Conectar vistas
        emailField = findViewById(R.id.etEmail)
        passwordField = findViewById(R.id.etPassword)
        loginButton = findViewById(R.id.btnLogin)
        registerLink = findViewById(R.id.tvRegisterLink)
        progressBar = findViewById(R.id.progressBar)

        // Click en login
        loginButton.setOnClickListener {
            val email = emailField.text.toString().trim()
            val password = passwordField.text.toString().trim()

            if (validateInput(email, password)) {
                performLogin(email, password)
            }
        }

        // Click en "crear cuenta"
        registerLink.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun validateInput(email: String, password: String): Boolean {
        if (email.isEmpty()) {
            emailField.error = "Email requerido"
            return false
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailField.error = "Email inválido"
            return false
        }

        if (password.isEmpty()) {
            passwordField.error = "Contraseña requerida"
            return false
        }

        if (password.length < 6) {
            passwordField.error = "Mínimo 6 caracteres"
            return false
        }

        return true
    }

    private fun performLogin(email: String, password: String) {
        setLoading(true)

        lifecycleScope.launch {
            try {
                val response = apiRepository.login(email, password)

                if (response.isSuccessful && response.body() != null) {
                    val authResponse = response.body()!!

                    // Guardar sesión
                    sessionManager.saveSession(
                        token = authResponse.session.accessToken,
                        userId = authResponse.user.id,
                        email = authResponse.user.email,
                        username = authResponse.user.userMetadata?.username
                    )

                    // Configurar token en Retrofit
                    RetrofitClient.setAuthToken(authResponse.session.accessToken)

                    Toast.makeText(
                        this@LoginActivity,
                        "¡Bienvenido!",
                        Toast.LENGTH_SHORT
                    ).show()

                    goToMainActivity()
                } else {
                    val errorBody = response.errorBody()?.string()
                    Toast.makeText(
                        this@LoginActivity,
                        "Error: ${errorBody ?: "Login fallido"}",
                        Toast.LENGTH_LONG
                    ).show()
                }

            } catch (e: Exception) {
                Toast.makeText(
                    this@LoginActivity,
                    "Error de conexión: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                setLoading(false)
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        loginButton.isEnabled = !loading
        emailField.isEnabled = !loading
        passwordField.isEnabled = !loading
    }

    private fun goToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}