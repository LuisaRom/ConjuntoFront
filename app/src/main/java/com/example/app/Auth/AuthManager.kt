package com.example.app.Auth

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.app.Model.Usuario
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object AuthManager {
    private const val PREF_NAME = "auth_secure_prefs"
    private const val KEY_TOKEN = "jwt_token"
    private const val KEY_USER = "auth_user"

    private val gson = Gson()

    @Volatile
    private var isInitialized = false
    private lateinit var appContext: Context

    private val _currentUser = MutableStateFlow<Usuario?>(null)
    val currentUser: StateFlow<Usuario?> = _currentUser.asStateFlow()

    private val _authMessage = MutableStateFlow<String?>(null)
    val authMessage: StateFlow<String?> = _authMessage.asStateFlow()

    fun init(context: Context) {
        if (isInitialized) return
        synchronized(this) {
            if (isInitialized) return
            appContext = context.applicationContext
            _currentUser.value = getUser()
            isInitialized = true
        }
    }

    fun saveSession(token: String, usuario: Usuario) {
        val prefs = securePrefs()
        prefs.edit()
            .putString(KEY_TOKEN, token)
            .putString(KEY_USER, gson.toJson(usuario))
            .apply()
        _currentUser.value = usuario
    }

    fun logout() {
        if (!isInitialized) return
        securePrefs().edit().clear().apply()
        _currentUser.value = null
    }

    fun getToken(): String? {
        if (!isInitialized) return null
        return securePrefs().getString(KEY_TOKEN, null)
    }

    fun getUser(): Usuario? {
        if (!::appContext.isInitialized) return null
        val userJson = securePrefs().getString(KEY_USER, null) ?: return null
        return runCatching { gson.fromJson(userJson, Usuario::class.java) }.getOrNull()
    }

    fun isAuthenticated(): Boolean = !getToken().isNullOrBlank()

    fun handleUnauthorized() {
        logout()
        _authMessage.value = "Sesión expirada. Inicia sesión de nuevo."
    }

    fun handleForbidden() {
        _authMessage.value = "No tienes permisos"
    }

    fun clearAuthMessage() {
        _authMessage.value = null
    }

    private fun securePrefs() = EncryptedSharedPreferences.create(
        appContext,
        PREF_NAME,
        MasterKey.Builder(appContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
}
