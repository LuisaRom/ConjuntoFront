package com.example.app.ViewModel

import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.Auth.AuthManager
import com.example.app.Auth.TerminosSesion
import com.example.app.Model.TerminosCondiciones
import com.example.app.Repository.TerminosRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import javax.inject.Inject

@HiltViewModel
class TerminosViewModel @Inject constructor(
    private val repository: TerminosRepository
) : ViewModel() {

    private val _verificando = MutableStateFlow(false)
    val verificando: StateFlow<Boolean> = _verificando.asStateFlow()

    private val _enviando = MutableStateFlow(false)
    val enviando: StateFlow<Boolean> = _enviando.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _fechaAceptacion = MutableStateFlow<String?>(null)
    val fechaAceptacion: StateFlow<String?> = _fechaAceptacion.asStateFlow()

    fun verificar() {
        val usuario = AuthManager.currentUser.value ?: return
        if (TerminosSesion.estaAceptado(usuario.id, TerminosCondiciones.VERSION) || _verificando.value) return
        _verificando.value = true
        _error.value = null
        viewModelScope.launch {
            try {
                val estado = withContext(Dispatchers.IO) { repository.estado() }
                _fechaAceptacion.value = estado.fechaAceptacion
                if (estado.aceptado == true && estado.versionVigente == TerminosCondiciones.VERSION) {
                    TerminosSesion.marcarAceptado(usuario.id, TerminosCondiciones.VERSION)
                }
            } catch (e: Exception) {
                _error.value = mensajeError(e, "No se pudo verificar la aceptación de los términos")
            } finally {
                _verificando.value = false
            }
        }
    }

    fun cargarEstado() {
        viewModelScope.launch {
            runCatching { withContext(Dispatchers.IO) { repository.estado() } }
                .onSuccess { _fechaAceptacion.value = it.fechaAceptacion }
        }
    }

    fun aceptar() {
        val usuario = AuthManager.currentUser.value ?: return
        _enviando.value = true
        _error.value = null
        viewModelScope.launch {
            try {
                val estado = withContext(Dispatchers.IO) {
                    repository.aceptar("Android ${Build.VERSION.RELEASE} - ${Build.MANUFACTURER} ${Build.MODEL}")
                }
                if (estado.aceptado == true) {
                    TerminosSesion.marcarAceptado(usuario.id, TerminosCondiciones.VERSION)
                } else {
                    _error.value = "No se pudo registrar la aceptación. Intenta de nuevo."
                }
            } catch (e: Exception) {
                _error.value = mensajeError(e, "No se pudo registrar la aceptación")
            } finally {
                _enviando.value = false
            }
        }
    }

    private fun mensajeError(e: Exception, base: String): String {
        if (e is HttpException) {
            val cuerpo = runCatching { e.response()?.errorBody()?.string() }.getOrNull().orEmpty()
            val detalle = Regex("\"(?:mensaje|message|error)\"\\s*:\\s*\"([^\"]+)\"").find(cuerpo)?.groupValues?.get(1)
            return detalle ?: "$base (HTTP ${e.code()})"
        }
        return "$base: revisa tu conexión e intenta de nuevo."
    }
}
