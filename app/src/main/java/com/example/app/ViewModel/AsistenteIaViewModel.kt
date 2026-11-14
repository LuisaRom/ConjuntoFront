package com.example.app.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.Model.ConversacionIA
import com.example.app.Model.EstadoIA
import com.example.app.Model.MensajeIA
import com.example.app.Repository.IaRepository
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
class AsistenteIaViewModel @Inject constructor(
    private val repository: IaRepository
) : ViewModel() {

    private val _mensajes = MutableStateFlow<List<MensajeIA>>(emptyList())
    val mensajes: StateFlow<List<MensajeIA>> = _mensajes.asStateFlow()

    private val _conversaciones = MutableStateFlow<List<ConversacionIA>>(emptyList())
    val conversaciones: StateFlow<List<ConversacionIA>> = _conversaciones.asStateFlow()

    private val _conversacionId = MutableStateFlow<Long?>(null)
    val conversacionId: StateFlow<Long?> = _conversacionId.asStateFlow()

    private val _estado = MutableStateFlow<EstadoIA?>(null)
    val estado: StateFlow<EstadoIA?> = _estado.asStateFlow()

    private val _escribiendo = MutableStateFlow(false)
    val escribiendo: StateFlow<Boolean> = _escribiendo.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _aviso = MutableStateFlow<String?>(null)
    val aviso: StateFlow<String?> = _aviso.asStateFlow()

    init {
        cargarEstado()
        cargarConversaciones()
    }

    fun cargarEstado() = viewModelScope.launch {
        runCatching { withContext(Dispatchers.IO) { repository.estado() } }
            .onSuccess { _estado.value = it }
    }

    fun cargarConversaciones() = viewModelScope.launch {
        runCatching { withContext(Dispatchers.IO) { repository.conversaciones() } }
            .onSuccess { _conversaciones.value = it }
    }

    fun nuevaConversacion() {
        _conversacionId.value = null
        _mensajes.value = emptyList()
        _error.value = null
    }

    fun abrirConversacion(id: Long) = viewModelScope.launch {
        _error.value = null
        try {
            val lista = withContext(Dispatchers.IO) { repository.mensajes(id) }
            _conversacionId.value = id
            _mensajes.value = lista
        } catch (e: Exception) {
            _error.value = mensajeError(e, "No se pudo abrir la conversación")
        }
    }

    fun eliminarConversacion(id: Long) = viewModelScope.launch {
        try {
            withContext(Dispatchers.IO) { repository.eliminarConversacion(id) }
            if (_conversacionId.value == id) nuevaConversacion()
            cargarConversaciones()
        } catch (e: Exception) {
            _error.value = mensajeError(e, "No se pudo eliminar la conversación")
        }
    }

    fun enviar(texto: String) {
        val limpio = texto.trim()
        if (limpio.isEmpty() || _escribiendo.value) return
        _error.value = null
        _mensajes.value = _mensajes.value + MensajeIA(rol = "USUARIO", contenido = limpio)
        _escribiendo.value = true
        viewModelScope.launch {
            try {
                val respuesta = withContext(Dispatchers.IO) {
                    repository.enviarMensaje(limpio, _conversacionId.value)
                }
                val esNueva = _conversacionId.value == null
                _conversacionId.value = respuesta.conversacionId
                _mensajes.value = _mensajes.value + MensajeIA(
                    id = respuesta.mensajeId,
                    rol = "ASISTENTE",
                    contenido = respuesta.respuesta.orEmpty()
                )
                if (esNueva) cargarConversaciones()
            } catch (e: Exception) {
                _error.value = mensajeError(e, "No se pudo enviar el mensaje")
            } finally {
                _escribiendo.value = false
            }
        }
    }

    fun calificar(mensajeId: Long, util: Boolean) = viewModelScope.launch {
        try {
            val aprendido = withContext(Dispatchers.IO) { repository.calificar(mensajeId, util) }
            _mensajes.value = _mensajes.value.map { if (it.id == mensajeId) it.copy(util = util) else it }
            _aviso.value = when {
                aprendido -> "Gracias. La respuesta se propuso a la administración como conocimiento."
                util -> "Gracias por tu calificación"
                else -> "Gracias. Usaremos tu calificación para mejorar."
            }
        } catch (e: Exception) {
            _error.value = mensajeError(e, "No se pudo registrar la calificación")
        }
    }

    fun limpiarAviso() {
        _aviso.value = null
    }

    fun limpiarError() {
        _error.value = null
    }

    private fun mensajeError(e: Exception, base: String): String {
        if (e is HttpException) {
            val cuerpo = runCatching { e.response()?.errorBody()?.string() }.getOrNull().orEmpty()
            val detalle = Regex("\"(?:mensaje|message|error)\"\\s*:\\s*\"([^\"]+)\"").find(cuerpo)?.groupValues?.get(1)
            return detalle ?: "$base (HTTP ${e.code()})"
        }
        return "$base: ${e.message ?: "error de conexión"}"
    }
}
