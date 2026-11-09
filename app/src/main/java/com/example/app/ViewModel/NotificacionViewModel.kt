package com.example.app.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.Model.Notificacion
import com.example.app.Repository.NotificacionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class NotificacionViewModel @Inject constructor(
    private val repository: NotificacionRepository
) : ViewModel() {

    private val _notificaciones = MutableStateFlow<List<Notificacion>>(emptyList())
    val notificaciones: StateFlow<List<Notificacion>> = _notificaciones.asStateFlow()

    private val _mensajesChat = MutableStateFlow<List<Notificacion>>(emptyList())
    val mensajesChat: StateFlow<List<Notificacion>> = _mensajesChat.asStateFlow()

    private val _notificacionSeleccionada = MutableStateFlow<Notificacion?>(null)
    val notificacionSeleccionada: StateFlow<Notificacion?> = _notificacionSeleccionada.asStateFlow()

    private val _misRecibos = MutableStateFlow<List<Notificacion>>(emptyList())
    val misRecibos: StateFlow<List<Notificacion>> = _misRecibos.asStateFlow()

    private val _cargandoMisRecibos = MutableStateFlow(false)
    val cargandoMisRecibos: StateFlow<Boolean> = _cargandoMisRecibos.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Contador de notificaciones no leídas
    private val _notificacionesNoLeidas = MutableStateFlow(0)
    val notificacionesNoLeidas: StateFlow<Int> = _notificacionesNoLeidas.asStateFlow()

    init {
        // No llamar obtenerTodos() aquí para evitar crashes al inicializar
        // Se llamará desde la UI cuando sea necesario
    }

    fun obtenerNovedades(search: String? = null) = viewModelScope.launch {
        _isLoading.value = true
        _error.value = null
        try {
            android.util.Log.d("NotificacionViewModel", "Obteniendo novedades...")
            val lista = withContext(Dispatchers.IO) {
                repository.obtenerNovedades(search)
            }
            aplicarListaNovedades(lista)
        } catch (e: Exception) {
            android.util.Log.e("NotificacionViewModel", "Error al obtener novedades", e)
            _error.value = "Error al obtener novedades: ${e.message}"
            _notificaciones.value = emptyList()
        } finally {
            _isLoading.value = false
        }
    }

    fun obtenerTodos() = viewModelScope.launch {
        _isLoading.value = true
        _error.value = null
        try {
            android.util.Log.d("NotificacionViewModel", "Obteniendo novedades (feed)...")
            val lista = withContext(Dispatchers.IO) {
                repository.obtenerNovedades()
            }
            aplicarListaNovedades(lista)
        } catch (e: Exception) {
            android.util.Log.e("NotificacionViewModel", "Error al obtener novedades", e)
            _error.value = "Error al obtener novedades: ${e.message}"
            _notificaciones.value = emptyList()
        } finally {
            _isLoading.value = false
        }
    }

    fun obtenerTodosSilencioso() = viewModelScope.launch {
        try {
            val lista = withContext(Dispatchers.IO) {
                repository.obtenerNovedades()
            }
            val listaValida = lista.filter { it.esValida() }
            if (_notificaciones.value != listaValida) {
                _notificaciones.value = listaValida
            }
            _notificacionesNoLeidas.value = listaValida.size
            _error.value = null
        } catch (e: Exception) {
            android.util.Log.e("NotificacionViewModel", "Error en refresco silencioso", e)
        }
    }

    private fun aplicarListaNovedades(lista: List<Notificacion>) {
        android.util.Log.d("NotificacionViewModel", "Novedades recibidas: ${lista.size}")
        val listaValida = lista.filter { notificacion ->
            val esValida = notificacion.esValida()
            if (!esValida) {
                android.util.Log.w(
                    "NotificacionViewModel",
                    "Novedad inválida filtrada: id=${notificacion.id}, mensaje=${notificacion.mensaje}"
                )
            }
            esValida
        }
        android.util.Log.d("NotificacionViewModel", "Novedades válidas: ${listaValida.size}")
        _notificaciones.value = listaValida
        _notificacionesNoLeidas.value = listaValida.size
    }

    /** Recibos del residente autenticado. [silencioso] = refresco sin spinner ni errores en pantalla. */
    fun cargarMisRecibos(silencioso: Boolean = false) = viewModelScope.launch {
        if (!silencioso) _cargandoMisRecibos.value = true
        try {
            _misRecibos.value = withContext(Dispatchers.IO) { repository.obtenerMisRecibos() }
            if (!silencioso) _error.value = null
        } catch (e: Exception) {
            android.util.Log.e("NotificacionViewModel", "Error al obtener mis recibos", e)
            if (!silencioso) _error.value = e.message
        } finally {
            _cargandoMisRecibos.value = false
        }
    }

    fun obtenerHistorialChat() = viewModelScope.launch {
        _isLoading.value = true
        _error.value = null
        try {
            val lista = withContext(Dispatchers.IO) {
                repository.obtenerHistorialChat()
            }
            _mensajesChat.value = lista.filter { it.esValida() }
        } catch (e: Exception) {
            android.util.Log.e("NotificacionViewModel", "Error al obtener mensajes de chat", e)
            _error.value = "Error al obtener mensajes: ${e.message}"
            _mensajesChat.value = emptyList()
        } finally {
            _isLoading.value = false
        }
    }

    fun obtenerHistorialChatSilencioso() = viewModelScope.launch {
        try {
            val lista = withContext(Dispatchers.IO) {
                repository.obtenerHistorialChat()
            }.filter { it.esValida() }
            if (_mensajesChat.value != lista) {
                _mensajesChat.value = lista
            }
        } catch (e: Exception) {
            android.util.Log.e("NotificacionViewModel", "Error en refresco silencioso del chat", e)
        }
    }

    fun enviarMensajeChat(
        destinatarioId: Long,
        mensaje: String,
        onSuccess: (() -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) = viewModelScope.launch {
        _isLoading.value = true
        _error.value = null
        try {
            val enviado = withContext(Dispatchers.IO) {
                repository.enviarMensajeChat(destinatarioId, mensaje)
            }
            _mensajesChat.value = (_mensajesChat.value + enviado).distinctBy { it.id ?: it.hashCode().toLong() }
            obtenerHistorialChatSilencioso()
            onSuccess?.invoke()
        } catch (e: Exception) {
            android.util.Log.e("NotificacionViewModel", "Error al enviar mensaje de chat", e)
            val textoError = "Error al enviar mensaje: ${e.message}"
            _error.value = textoError
            onError?.invoke(textoError)
        } finally {
            _isLoading.value = false
        }
    }

    suspend fun enviarRecibo(destinatarioId: Long, tipoRecibo: String) {
        withContext(Dispatchers.IO) {
            repository.enviarRecibo(destinatarioId, tipoRecibo)
        }
    }

    fun enviarReciboResidente(
        destinatarioId: Long,
        tipoRecibo: String,
        onSuccess: (() -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) = viewModelScope.launch {
        _isLoading.value = true
        _error.value = null
        try {
            enviarRecibo(destinatarioId, tipoRecibo)
            onSuccess?.invoke()
        } catch (e: Exception) {
            val mensaje = "Error al enviar recibo: ${e.message}"
            _error.value = mensaje
            onError?.invoke(mensaje)
        } finally {
            _isLoading.value = false
        }
    }

    fun guardar(
        notificacion: Notificacion,
        onSuccess: (() -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) = viewModelScope.launch {
        _isLoading.value = true
        _error.value = null
        try {
            android.util.Log.d("NotificacionViewModel", "Guardando notificación: mensaje=${notificacion.mensaje}, usuario=${notificacion.usuario?.id}, fecha=${notificacion.fechaEnvio}")
            val notificacionGuardada = withContext(Dispatchers.IO) {
                repository.guardar(notificacion)
            }
            android.util.Log.d("NotificacionViewModel", "Notificación guardada exitosamente: id=${notificacionGuardada.id}")
            kotlinx.coroutines.delay(300)
            obtenerNovedades()
            onSuccess?.invoke()
        } catch (e: Exception) {
            android.util.Log.e("NotificacionViewModel", "Error al guardar notificación", e)
            val mensaje = limpiarMensajeError(e.message)
            _error.value = mensaje
            onError?.invoke(mensaje)
        } finally {
            _isLoading.value = false
        }
    }

    fun guardarConMultimedia(
        notificacion: Notificacion,
        imagenFile: java.io.File?,
        videoFile: java.io.File?,
        onSuccess: (() -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) = viewModelScope.launch {
        _isLoading.value = true
        _error.value = null
        try {
            val notificacionGuardada = withContext(Dispatchers.IO) {
                repository.guardarConMultimedia(notificacion, imagenFile, videoFile)
            }
            android.util.Log.d("NotificacionViewModel", "Publicación con multimedia guardada: id=${notificacionGuardada.id}, imagen=${notificacionGuardada.imagenUrl}")
            kotlinx.coroutines.delay(300)
            obtenerNovedades()
            onSuccess?.invoke()
        } catch (e: Exception) {
            android.util.Log.e("NotificacionViewModel", "Error al guardar publicación con multimedia", e)
            val mensaje = limpiarMensajeError(e.message)
            _error.value = mensaje
            onError?.invoke(mensaje)
        } finally {
            _isLoading.value = false
        }
    }

    fun eliminar(id: Long) = viewModelScope.launch {
        _isLoading.value = true
        _error.value = null
        try {
            withContext(Dispatchers.IO) {
                repository.eliminar(id)
            }
            obtenerNovedades()
        } catch (e: Exception) {
            _error.value = "Error al eliminar notificación: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    fun actualizar(id: Long, notificacion: Notificacion) = viewModelScope.launch {
        _isLoading.value = true
        _error.value = null
        try {
            val notificacionActualizada = withContext(Dispatchers.IO) {
                repository.actualizar(id, notificacion)
            }
            obtenerNovedades()
        } catch (e: Exception) {
            _error.value = "Error al actualizar notificación: ${e.message}"
            throw e
        } finally {
            _isLoading.value = false
        }
    }

    private fun limpiarMensajeError(mensaje: String?): String {
        val base = mensaje?.trim().orEmpty()
        if (base.isBlank()) return "No se pudo guardar la publicación"
        return base
            .removePrefix("Error al guardar publicación: ")
            .removePrefix("Error al guardar notificación: ")
    }

    fun obtenerPorId(id: Long) = viewModelScope.launch {
        _isLoading.value = true
        _error.value = null
        try {
            val notificacion = withContext(Dispatchers.IO) {
                repository.obtenerPorId(id)
            }
            _notificacionSeleccionada.value = notificacion
        } catch (e: Exception) {
            _error.value = "Error al obtener notificación: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    fun clearError() {
        _error.value = null
    }
}