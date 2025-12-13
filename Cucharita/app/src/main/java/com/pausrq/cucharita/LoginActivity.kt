// 📁 app/src/main/java/com/pausrq/cucharita/LoginActivity.kt
package com.pausrq.cucharita

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.pausrq.cucharita.api.ApiClient
import com.pausrq.cucharita.api.LoginRequest
import com.pausrq.cucharita.api.RegisterRequest
import com.pausrq.cucharita.storage.SessionManager
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var nameInput: EditText
    private lateinit var loginButton: Button
    private lateinit var registerButton: Button
    private lateinit var toggleModeText: TextView

    private lateinit var sessionManager: SessionManager
    private val apiService = ApiClient.getApiService()

    private var isLoginMode = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        sessionManager = SessionManager(this)

        // Verificar si ya hay sesión activa
        if (sessionManager.isLoggedIn()) {
            goToMainActivity()
            return
        }

        // Conectar vistas
        emailInput = findViewById(R.id.emailInput)
        passwordInput = findViewById(R.id.passwordInput)
        nameInput = findViewById(R.id.nameInput)
        loginButton = findViewById(R.id.loginButton)
        registerButton = findViewById(R.id.registerButton)
        toggleModeText = findViewById(R.id.toggleModeText)

        updateUI()

        loginButton.setOnClickListener {
            if (isLoginMode) {
                performLogin()
            } else {
                performRegister()
            }
        }

        registerButton.setOnClickListener {
            isLoginMode = !isLoginMode
            updateUI()
        }

        toggleModeText.setOnClickListener {
            isLoginMode = !isLoginMode
            updateUI()
        }
    }

    private fun updateUI() {
        if (isLoginMode) {
            nameInput.visibility = android.view.View.GONE
            loginButton.text = "Iniciar Sesión"
            registerButton.text = "¿No tienes cuenta? Regístrate"
            toggleModeText.text = "Crear nueva cuenta"
        } else {
            nameInput.visibility = android.view.View.VISIBLE
            loginButton.text = "Registrarse"
            registerButton.text = "¿Ya tienes cuenta? Inicia sesión"
            toggleModeText.text = "Volver a iniciar sesión"
        }
    }

    private fun performLogin() {
        val email = emailInput.text.toString().trim()
        val password = passwordInput.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        loginButton.isEnabled = false

        lifecycleScope.launch {
            try {
                val response = apiService.login(LoginRequest(email, password))

                if (response.isSuccessful && response.body() != null) {
                    val authResponse = response.body()!!

                    // Guardar token y datos del usuario
                    authResponse.access_token?.let { sessionManager.saveToken(it) }
                    sessionManager.saveUserData(
                        userId = authResponse.user.id,
                        email = authResponse.user.email,
                        name = authResponse.user.user_metadata?.name
                    )

                    Toast.makeText(this@LoginActivity, "¡Bienvenido!", Toast.LENGTH_SHORT).show()
                    goToMainActivity()
                } else {
                    Toast.makeText(
                        this@LoginActivity,
                        "Credenciales incorrectas",
                        Toast.LENGTH_SHORT
                    ).show()
                    loginButton.isEnabled = true
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@LoginActivity,
                    "Error de conexión: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                loginButton.isEnabled = true
            }
        }
    }

    private fun performRegister() {
        val email = emailInput.text.toString().trim()
        val password = passwordInput.text.toString().trim()
        val name = nameInput.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.length < 6) {
            Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
            return
        }

        loginButton.isEnabled = false

        lifecycleScope.launch {
            try {
                val response = apiService.register(
                    RegisterRequest(email, password, name.ifEmpty { null })
                )

                if (response.isSuccessful && response.body() != null) {
                    val authResponse = response.body()!!

                    // Guardar token y datos del usuario
                    authResponse.session?.access_token?.let { sessionManager.saveToken(it) }
                    sessionManager.saveUserData(
                        userId = authResponse.user.id,
                        email = authResponse.user.email,
                        name = authResponse.user.user_metadata?.name
                    )

                    Toast.makeText(this@LoginActivity, "¡Cuenta creada exitosamente!", Toast.LENGTH_SHORT).show()
                    goToMainActivity()
                } else {
                    Toast.makeText(
                        this@LoginActivity,
                        "Error al crear cuenta. Puede que el email ya exista.",
                        Toast.LENGTH_SHORT
                    ).show()
                    loginButton.isEnabled = true
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@LoginActivity,
                    "Error de conexión: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                loginButton.isEnabled = true
            }
        }
    }

    private fun goToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}