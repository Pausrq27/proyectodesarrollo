package com.pausrq.cucharita

import RetrofitClient
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
import com.pausrq.cucharita.utils.SessionManager
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var usernameField: EditText
    private lateinit var emailField: EditText
    private lateinit var passwordField: EditText
    private lateinit var confirmPasswordField: EditText
    private lateinit var registerButton: Button
    private lateinit var loginLink: TextView
    private lateinit var progressBar: ProgressBar

    private lateinit var sessionManager: SessionManager
    private val apiRepository = ApiRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        sessionManager = SessionManager(this)

        // Conectar vistas
        usernameField = findViewById(R.id.etUsername)
        emailField = findViewById(R.id.etEmail)
        passwordField = findViewById(R.id.etPassword)
        confirmPasswordField = findViewById(R.id.etConfirmPassword)
        registerButton = findViewById(R.id.btnRegister)
        loginLink = findViewById(R.id.tvLoginLink)
        progressBar = findViewById(R.id.progressBar)

        // Click en registrar
        registerButton.setOnClickListener {
            val username = usernameField.text.toString().trim()
            val email = emailField.text.toString().trim()
            val password = passwordField.text.toString()
            val confirmPassword = confirmPasswordField.text.toString()

            if (validateInput(username, email, password, confirmPassword)) {
                performRegister(username, email, password)
            }
        }

        // Click en "ya tengo cuenta"
        loginLink.setOnClickListener {
            finish() // Volver a login
        }
    }

    private fun validateInput(
        username: String,
        email: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        if (username.isEmpty()) {
            usernameField.error = "Usuario requerido"
            return false
        }

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

        if (password != confirmPassword) {
            confirmPasswordField.error = "Las contraseñas no coinciden"
            return false
        }

        return true
    }

    private fun performRegister(username: String, email: String, password: String) {
        setLoading(true)

        lifecycleScope.launch {
            try {
                val response = apiRepository.register(
                    email = email,
                    password = password,
                    username = username,
                    fullName = null
                )

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
                        this@RegisterActivity,
                        "¡Cuenta creada exitosamente!",
                        Toast.LENGTH_SHORT
                    ).show()

                    goToMainActivity()
                } else {
                    val errorBody = response.errorBody()?.string()
                    Toast.makeText(
                        this@RegisterActivity,
                        "Error: ${errorBody ?: "Registro fallido"}",
                        Toast.LENGTH_LONG
                    ).show()
                }

            } catch (e: Exception) {
                Toast.makeText(
                    this@RegisterActivity,
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
        registerButton.isEnabled = !loading
        usernameField.isEnabled = !loading
        emailField.isEnabled = !loading
        passwordField.isEnabled = !loading
        confirmPasswordField.isEnabled = !loading
    }

    private fun goToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}