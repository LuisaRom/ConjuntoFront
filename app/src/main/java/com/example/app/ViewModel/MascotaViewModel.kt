package com.example.app.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.Model.Mascota
import com.example.app.Repository.MascotaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import retrofit2.HttpException

@HiltViewModel
class MascotaViewModel @Inject constructor(
    private val repository: MascotaRepository
) : ViewModel() {
    
    private val _mascotas = MutableStateFlow<List<Mascota>>(emptyList())
    val mascotas: StateFlow<List<Mascota>> = _mascotas.asStateFlow()

    private val _mascotaSeleccionada = MutableStateFlow<Mascota?>(null)
    val mascotaSeleccionada: StateFlow<Mascota?> = _mascotaSeleccionada.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun obtenerTodos() = viewModelScope.launch {
        _isLoading.value = true
        _error.value = null
        try {
            val lista = withContext(Dispatchers.IO) {
                repository.obtenerTodos()
            }
            _mascotas.value = lista
        } catch (e: Exception) {
            _error.value = "Error al obtener mascotas: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    fun guardar(mascota: Mascota, onSuccess: (() -> Unit)? = null) = viewModelScope.launch {
        _isLoading.value = true
        _error.value = null
        try {
            val mascotaGuardada = withContext(Dispatchers.IO) {
                repository.guardar(mascota)
            }
            _mascotas.value = listOf(mascotaGuardada) + _mascotas.value.filterNot { it.id == mascotaGuardada.id }
            obtenerTodos()
            onSuccess?.invoke()
        } catch (e: Exception) {
            android.util.Log.e("MascotaViewModel", "Error al guardar mascota", e)
            _error.value = when (e) {
                is HttpException -> "Error HTTP ${e.code()} al guardar mascota"
                else -> "Error al guardar mascota: ${e.message}"
            }
        } finally {
            _isLoading.value = false
        }
    }

    fun guardarConFoto(mascota: Mascota, fotoFile: File, onSuccess: (() -> Unit)? = null) = viewModelScope.launch {
        _isLoading.value = true
        _error.value = null
        try {
            val mascotaGuardada = withContext(Dispatchers.IO) {
                repository.guardarConFoto(mascota, fotoFile)
            }
            _mascotas.value = listOf(mascotaGuardada) + _mascotas.value.filterNot { it.id == mascotaGuardada.id }
            obtenerTodos()
            onSuccess?.invoke()
        } catch (e: Exception) {
            android.util.Log.e("MascotaViewModel", "Error al guardar mascota con foto", e)
            if (e is HttpException && e.code() == 400) {
                // Fallback para backends que rechazan multipart: crear publicación sin foto.
                runCatching {
                    withContext(Dispatchers.IO) { repository.guardar(mascota) }
                }.onSuccess { mascotaGuardada ->
                    _mascotas.value = listOf(mascotaGuardada) + _mascotas.value.filterNot { it.id == mascotaGuardada.id }
                    obtenerTodos()
                    onSuccess?.invoke()
                    return@launch
                }.onFailure {
                    _error.value = "No se pudo crear la publicación de mascota. Intenta de nuevo."
                }
            } else {
                _error.value = when (e) {
                    is HttpException -> "Error HTTP ${e.code()} al guardar mascota con foto"
                    else -> "Error al guardar mascota con foto: ${e.message}"
                }
            }
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
            obtenerTodos()
        } catch (e: Exception) {
            _error.value = "Error al eliminar mascota: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    fun obtenerPorId(id: Long) = viewModelScope.launch {
        _isLoading.value = true
        _error.value = null
        try {
            val mascota = withContext(Dispatchers.IO) {
                repository.obtenerPorId(id)
            }
            _mascotaSeleccionada.value = mascota
        } catch (e: Exception) {
            _error.value = "Error al obtener mascota: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    fun clearError() {
        _error.value = null
    }
}