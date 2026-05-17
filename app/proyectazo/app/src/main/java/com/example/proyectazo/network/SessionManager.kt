package com.example.proyectazo.network

import android.content.Context

class SessionManager(context: Context) {

    private val prefs = context.getSharedPreferences("smartfit_session", Context.MODE_PRIVATE)

    fun guardarSesion(token: String, userId: Int, nombre: String) {
        prefs.edit()
            .putString("token", token)
            .putInt("user_id", userId)
            .putString("user_nombre", nombre)
            .apply()
    }

    fun getUserNombre(): String = prefs.getString("user_nombre", "Usuario") ?: "Usuario"
    fun getToken(): String? = prefs.getString("token", null)

    fun getUserId(): Int = prefs.getInt("user_id", -1)

    fun isLoggedIn(): Boolean = getToken() != null

    fun cerrarSesion() {
        prefs.edit().clear().apply()
    }
}