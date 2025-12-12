package com.pausrq.cucharita.utils

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        "cucharita_session",
        Context.MODE_PRIVATE
    )

    companion object {
        private const val KEY_TOKEN = "access_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_EMAIL = "email"
        private const val KEY_USERNAME = "username"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
    }

    // Guardar sesión
    fun saveSession(
        token: String,
        userId: String,
        email: String,
        username: String?
    ) {
        prefs.edit().apply {
            putString(KEY_TOKEN, token)
            putString(KEY_USER_ID, userId)
            putString(KEY_EMAIL, email)
            putString(KEY_USERNAME, username)
            putBoolean(KEY_IS_LOGGED_IN, true)
            apply()
        }
    }

    // Obtener token
    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)

    // Obtener user ID
    fun getUserId(): String? = prefs.getString(KEY_USER_ID, null)

    // Obtener email
    fun getEmail(): String? = prefs.getString(KEY_EMAIL, null)

    // Obtener username
    fun getUsername(): String? = prefs.getString(KEY_USERNAME, null)

    // Verificar si está logueado
    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_IS_LOGGED_IN, false)

    // Cerrar sesión
    fun clearSession() {
        prefs.edit().clear().apply()
    }
}