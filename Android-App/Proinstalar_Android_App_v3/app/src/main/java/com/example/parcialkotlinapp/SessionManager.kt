package com.example.parcialkotlinapp

import android.content.Context
import android.content.SharedPreferences
import com.example.parcialkotlinapp.models.AuthResponse

/** Guarda la sesión del usuario registrado (token JWT + datos básicos). */
object SessionManager {
    private var prefs: SharedPreferences? = null

    fun init(context: Context) {
        prefs = context.applicationContext.getSharedPreferences("proinstalar_session", Context.MODE_PRIVATE)
    }

    val token: String? get() = prefs?.getString("token", null)
    val nombre: String get() = prefs?.getString("nombre", "") ?: ""
    val email: String get() = prefs?.getString("email", "") ?: ""
    val telefono: String get() = prefs?.getString("telefono", "") ?: ""
    val rol: String get() = prefs?.getString("rol", "") ?: ""

    val isAdmin: Boolean get() = rol.equals("ADMIN", ignoreCase = true)
    val direccion: String get() = prefs?.getString("direccion", "") ?: ""

    val isLoggedIn: Boolean get() = !token.isNullOrEmpty()

    fun guardar(r: AuthResponse) {
        prefs?.edit()
            ?.putString("token", r.token)
            ?.putString("nombre", r.nombre)
            ?.putString("email", r.email)
            ?.putString("telefono", r.telefono ?: "")
            ?.putString("direccion", r.direccion ?: "")
            ?.putString("rol", r.rol ?: "")
            ?.apply()
    }

    fun cerrarSesion() {
        prefs?.edit()?.clear()?.apply()
        CartManager.clear()
    }
}
